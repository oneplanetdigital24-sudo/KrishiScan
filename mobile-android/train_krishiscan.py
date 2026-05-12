#!/usr/bin/env python3
"""Train and export the KrishiScan TensorFlow Lite model from a shell."""

from __future__ import annotations

import argparse
import os
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path


SELECTED_CLASSES = [
    "Tomato___Bacterial_spot",
    "Tomato___Early_blight",
    "Tomato___Late_blight",
    "Tomato___Leaf_Mold",
    "Tomato___healthy",
    "Potato___Early_blight",
    "Potato___Late_blight",
    "Potato___healthy",
    "Rice___Brown_spot",
    "Rice___Leaf_blast",
    "Rice___healthy",
    "Corn___Common_rust",
    "Corn___Northern_Leaf_Blight",
    "Corn___healthy",
    "Pepper___Bacterial_spot",
]


def parse_args() -> argparse.Namespace:
    repo_mobile_dir = Path(__file__).resolve().parent
    parser = argparse.ArgumentParser(
        description="Download the plant disease dataset, train MobileNetV2, and export Android TFLite assets."
    )
    parser.add_argument(
        "--work-dir",
        type=Path,
        default=repo_mobile_dir / "training-work",
        help="Directory for downloaded data, prepared dataset, and exports.",
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=None,
        help="Where to write krishiscan_model.tflite and labels.txt. Defaults to WORK_DIR/export.",
    )
    parser.add_argument(
        "--android-assets-dir",
        type=Path,
        default=repo_mobile_dir / "app" / "src" / "main" / "assets",
        help="Android assets directory used with --copy-to-android-assets.",
    )
    parser.add_argument(
        "--dataset-slug",
        default="vipoooool/new-plant-diseases-dataset",
        help="Kaggle dataset slug.",
    )
    parser.add_argument(
        "--skip-download",
        action="store_true",
        help="Use an already extracted dataset under WORK_DIR/dataset.",
    )
    parser.add_argument("--batch-size", type=int, default=16)
    parser.add_argument("--epochs", type=int, default=8)
    parser.add_argument("--fine-tune-epochs", type=int, default=4)
    parser.add_argument("--fine-tune-layers", type=int, default=30)
    parser.add_argument("--image-size", type=int, default=224)
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument(
        "--copy-to-android-assets",
        action="store_true",
        help="Copy exported files into app/src/main/assets after training.",
    )
    return parser.parse_args()


def run(command: list[str]) -> None:
    print("+", " ".join(command), flush=True)
    subprocess.run(command, check=True)


def ensure_dataset(args: argparse.Namespace, dataset_dir: Path) -> None:
    if args.skip_download:
        if not dataset_dir.exists():
            raise FileNotFoundError(f"--skip-download was set, but {dataset_dir} does not exist")
        return

    args.work_dir.mkdir(parents=True, exist_ok=True)
    zip_path = args.work_dir / "new-plant-diseases-dataset.zip"
    if not zip_path.exists():
        run(
            [
                sys.executable,
                "-m",
                "kaggle",
                "datasets",
                "download",
                "-d",
                args.dataset_slug,
                "-p",
                str(args.work_dir),
            ]
        )

    dataset_dir.mkdir(parents=True, exist_ok=True)
    marker = dataset_dir / ".extracted"
    if not marker.exists():
        with zipfile.ZipFile(zip_path, "r") as archive:
            archive.extractall(dataset_dir)
        marker.write_text("ok\n", encoding="utf-8")


def find_train_valid_roots(dataset_dir: Path) -> tuple[Path, Path]:
    common_root = (
        dataset_dir
        / "New Plant Diseases Dataset(Augmented)"
        / "New Plant Diseases Dataset(Augmented)"
    )
    if (common_root / "train").exists():
        return common_root / "train", common_root / "valid"

    candidates = sorted(path for path in dataset_dir.rglob("train") if path.is_dir())
    if not candidates:
        raise RuntimeError(f"Train folder not found under {dataset_dir}")

    train_root = candidates[0]
    valid_root = train_root.parent / "valid"
    if not valid_root.exists():
        raise RuntimeError(f"Validation folder not found next to {train_root}")
    return train_root, valid_root


def prepare_subset(dataset_dir: Path, prepared_dir: Path) -> tuple[Path, Path]:
    train_root, valid_root = find_train_valid_roots(dataset_dir)
    out_train = prepared_dir / "train"
    out_valid = prepared_dir / "valid"
    out_train.mkdir(parents=True, exist_ok=True)
    out_valid.mkdir(parents=True, exist_ok=True)

    missing: list[str] = []
    for class_name in SELECTED_CLASSES:
        train_src = train_root / class_name
        valid_src = valid_root / class_name
        if train_src.exists():
            shutil.copytree(train_src, out_train / class_name, dirs_exist_ok=True)
        else:
            missing.append(f"train/{class_name}")
        if valid_src.exists():
            shutil.copytree(valid_src, out_valid / class_name, dirs_exist_ok=True)
        else:
            missing.append(f"valid/{class_name}")

    train_count = len([path for path in out_train.iterdir() if path.is_dir()])
    valid_count = len([path for path in out_valid.iterdir() if path.is_dir()])
    print(f"Train classes: {train_count}")
    print(f"Validation classes: {valid_count}")
    if missing:
        print("Missing selected folders:")
        for item in missing:
            print(f"  - {item}")
    if train_count == 0 or valid_count == 0:
        raise RuntimeError("Prepared dataset is empty")
    return out_train, out_valid


def build_datasets(args: argparse.Namespace, train_dir: Path, valid_dir: Path):
    img_size = (args.image_size, args.image_size)
    train_ds = tf.keras.utils.image_dataset_from_directory(
        train_dir,
        image_size=img_size,
        batch_size=args.batch_size,
        shuffle=True,
        seed=args.seed,
    )
    val_ds = tf.keras.utils.image_dataset_from_directory(
        valid_dir,
        image_size=img_size,
        batch_size=args.batch_size,
        shuffle=False,
    )
    class_names = train_ds.class_names
    print(f"Classes: {len(class_names)}")
    print(class_names)
    return train_ds.prefetch(tf.data.AUTOTUNE), val_ds.prefetch(tf.data.AUTOTUNE), class_names


def build_model(num_classes: int, image_size: int) -> tuple[tf.keras.Model, tf.keras.Model]:
    data_aug = tf.keras.Sequential(
        [
            layers.RandomFlip("horizontal"),
            layers.RandomRotation(0.08),
            layers.RandomZoom(0.1),
        ]
    )
    base = tf.keras.applications.MobileNetV2(
        input_shape=(image_size, image_size, 3),
        include_top=False,
        weights="imagenet",
    )
    base.trainable = False

    inputs = layers.Input(shape=(image_size, image_size, 3))
    x = data_aug(inputs)
    x = tf.keras.applications.mobilenet_v2.preprocess_input(x)
    x = base(x, training=False)
    x = layers.GlobalAveragePooling2D()(x)
    x = layers.Dropout(0.2)(x)
    outputs = layers.Dense(num_classes, activation="softmax")(x)
    return models.Model(inputs, outputs), base


def compile_model(model: tf.keras.Model, learning_rate: float) -> None:
    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate),
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"],
    )


def export_model(model: tf.keras.Model, class_names: list[str], output_dir: Path) -> None:
    output_dir.mkdir(parents=True, exist_ok=True)
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [tf.float16]
    tflite_model = converter.convert()

    model_path = output_dir / "krishiscan_model.tflite"
    labels_path = output_dir / "labels.txt"
    model_path.write_bytes(tflite_model)
    labels_path.write_text("\n".join(class_names) + "\n", encoding="utf-8")
    print(f"Exported: {model_path}")
    print(f"Exported: {labels_path}")


def copy_to_android_assets(output_dir: Path, android_assets_dir: Path) -> None:
    android_assets_dir.mkdir(parents=True, exist_ok=True)
    for filename in ("krishiscan_model.tflite", "labels.txt"):
        shutil.copy2(output_dir / filename, android_assets_dir / filename)
        print(f"Copied: {android_assets_dir / filename}")


def main() -> None:
    args = parse_args()
    global layers, models, tf
    import tensorflow as tf
    from tensorflow.keras import layers, models

    args.work_dir = args.work_dir.resolve()
    output_dir = (args.output_dir or args.work_dir / "export").resolve()
    dataset_dir = args.work_dir / "dataset"
    prepared_dir = args.work_dir / "krishiscan15"

    os.environ.setdefault("TF_CPP_MIN_LOG_LEVEL", "2")
    ensure_dataset(args, dataset_dir)
    train_dir, valid_dir = prepare_subset(dataset_dir, prepared_dir)
    train_ds, val_ds, class_names = build_datasets(args, train_dir, valid_dir)

    model, base = build_model(len(class_names), args.image_size)
    callbacks = [
        tf.keras.callbacks.EarlyStopping(
            monitor="val_accuracy",
            patience=3,
            restore_best_weights=True,
        )
    ]

    compile_model(model, 1e-3)
    model.fit(train_ds, validation_data=val_ds, epochs=args.epochs, callbacks=callbacks)

    if args.fine_tune_epochs > 0:
        base.trainable = True
        for layer in base.layers[:-args.fine_tune_layers]:
            layer.trainable = False
        compile_model(model, 1e-5)
        model.fit(
            train_ds,
            validation_data=val_ds,
            epochs=args.fine_tune_epochs,
            callbacks=callbacks,
        )

    export_model(model, class_names, output_dir)
    if args.copy_to_android_assets:
        copy_to_android_assets(output_dir, args.android_assets_dir.resolve())


if __name__ == "__main__":
    main()
