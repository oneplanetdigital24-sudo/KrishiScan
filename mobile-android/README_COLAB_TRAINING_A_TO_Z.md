# KrishiScan Model Training Guide (Colab Free Tier, A to Z)

This guide trains your **own 15-class model** for KrishiScan and exports:
- `krishiscan_model.tflite`
- `labels.txt`

Designed for **Google Colab free tier** with low-resource settings.

---

## 0. What You Will Build
You will train a lightweight MobileNetV2 classifier on your PRD 15 classes and convert it to TensorFlow Lite for Android.

Expected output classes (example names):
1. Tomato___Bacterial_spot
2. Tomato___Early_blight
3. Tomato___Late_blight
4. Tomato___Leaf_Mold
5. Tomato___healthy
6. Potato___Early_blight
7. Potato___Late_blight
8. Potato___healthy
9. Rice___Brown_spot
10. Rice___Leaf_blast
11. Rice___healthy
12. Corn___Common_rust
13. Corn___Northern_Leaf_Blight
14. Corn___healthy
15. Pepper___Bacterial_spot

---

## 1. Requirements
- Google account
- Kaggle account
- Kaggle API key (`kaggle.json`)
- Google Drive (for saving outputs)

---

## 2. Create Colab Notebook
1. Open `https://colab.research.google.com`
2. New Notebook
3. Runtime -> Change runtime type -> **GPU** (if available)

---

## 3. Colab Setup Cell
Run this first:

```python
!pip -q install kaggle tensorflow==2.15.0
```

Mount Drive:

```python
from google.colab import drive
drive.mount('/content/drive')
```

---

## 4. Kaggle API Setup
1. In Kaggle: Account -> Create New API Token
2. Upload `kaggle.json` to Colab (left panel -> Files -> Upload)

Then run:

```python
import os, shutil
os.makedirs('/root/.kaggle', exist_ok=True)
shutil.move('/content/kaggle.json', '/root/.kaggle/kaggle.json')
os.chmod('/root/.kaggle/kaggle.json', 0o600)
```

---

## 5. Download Dataset
Use PRD-aligned source:

```python
!kaggle datasets download -d vipoooool/new-plant-diseases-dataset -p /content
!unzip -q /content/new-plant-diseases-dataset.zip -d /content/dataset
```

---

## 6. Build 15-Class Subset (Important)
Run this cell and adjust names only if your downloaded folders differ:

```python
import os, shutil
from pathlib import Path

src_root = Path('/content/dataset')
# Try common path used by this dataset
if (src_root / 'New Plant Diseases Dataset(Augmented)' / 'New Plant Diseases Dataset(Augmented)' / 'train').exists():
    train_root = src_root / 'New Plant Diseases Dataset(Augmented)' / 'New Plant Diseases Dataset(Augmented)' / 'train'
    valid_root = src_root / 'New Plant Diseases Dataset(Augmented)' / 'New Plant Diseases Dataset(Augmented)' / 'valid'
else:
    # fallback: search for train/valid
    candidates = list(src_root.rglob('train'))
    train_root = candidates[0]
    valid_root = train_root.parent / 'valid'

selected = [
    'Tomato___Bacterial_spot',
    'Tomato___Early_blight',
    'Tomato___Late_blight',
    'Tomato___Leaf_Mold',
    'Tomato___healthy',
    'Potato___Early_blight',
    'Potato___Late_blight',
    'Potato___healthy',
    'Rice___Brown_spot',
    'Rice___Leaf_blast',
    'Rice___healthy',
    'Corn___Common_rust',
    'Corn___Northern_Leaf_Blight',
    'Corn___healthy',
    'Pepper___Bacterial_spot',
]

out_train = Path('/content/krishiscan15/train')
out_valid = Path('/content/krishiscan15/valid')
out_train.mkdir(parents=True, exist_ok=True)
out_valid.mkdir(parents=True, exist_ok=True)

for cls in selected:
    if (train_root / cls).exists():
        shutil.copytree(train_root / cls, out_train / cls, dirs_exist_ok=True)
    if (valid_root / cls).exists():
        shutil.copytree(valid_root / cls, out_valid / cls, dirs_exist_ok=True)

print('Train classes:', len([d for d in out_train.iterdir() if d.is_dir()]))
print('Valid classes:', len([d for d in out_valid.iterdir() if d.is_dir()]))
```

---

## 7. Train Lightweight Model (Free-Tier Friendly)
This config is tuned to be small and stable on free tier.

```python
import tensorflow as tf
from tensorflow.keras import layers, models

IMG_SIZE = (224, 224)
BATCH_SIZE = 16
SEED = 42

train_ds = tf.keras.utils.image_dataset_from_directory(
    '/content/krishiscan15/train',
    image_size=IMG_SIZE,
    batch_size=BATCH_SIZE,
    shuffle=True,
    seed=SEED
)

val_ds = tf.keras.utils.image_dataset_from_directory(
    '/content/krishiscan15/valid',
    image_size=IMG_SIZE,
    batch_size=BATCH_SIZE,
    shuffle=False
)

class_names = train_ds.class_names
num_classes = len(class_names)
print('Classes:', num_classes)
print(class_names)

AUTOTUNE = tf.data.AUTOTUNE
train_ds = train_ds.prefetch(AUTOTUNE)
val_ds = val_ds.prefetch(AUTOTUNE)

data_aug = tf.keras.Sequential([
    layers.RandomFlip('horizontal'),
    layers.RandomRotation(0.08),
    layers.RandomZoom(0.1),
])

base = tf.keras.applications.MobileNetV2(
    input_shape=(224, 224, 3),
    include_top=False,
    weights='imagenet'
)
base.trainable = False

inputs = layers.Input(shape=(224, 224, 3))
x = data_aug(inputs)
x = tf.keras.applications.mobilenet_v2.preprocess_input(x)
x = base(x, training=False)
x = layers.GlobalAveragePooling2D()(x)
x = layers.Dropout(0.2)(x)
outputs = layers.Dense(num_classes, activation='softmax')(x)
model = models.Model(inputs, outputs)

model.compile(
    optimizer=tf.keras.optimizers.Adam(1e-3),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

callbacks = [
    tf.keras.callbacks.EarlyStopping(monitor='val_accuracy', patience=3, restore_best_weights=True)
]

history = model.fit(train_ds, validation_data=val_ds, epochs=8, callbacks=callbacks)
```

Optional fine-tune (only if time allows):

```python
base.trainable = True
for layer in base.layers[:-30]:
    layer.trainable = False

model.compile(
    optimizer=tf.keras.optimizers.Adam(1e-5),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

history_ft = model.fit(train_ds, validation_data=val_ds, epochs=4, callbacks=callbacks)
```

---

## 8. Export TFLite + Labels

```python
import os

export_dir = '/content/krishiscan_export'
os.makedirs(export_dir, exist_ok=True)

# Save Keras model
model.save('/content/krishiscan_export/saved_model.keras')

# Convert to TFLite (float16 quantization for smaller size)
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.target_spec.supported_types = [tf.float16]
tflite_model = converter.convert()

with open('/content/krishiscan_export/krishiscan_model.tflite', 'wb') as f:
    f.write(tflite_model)

# Write labels in exact output order
with open('/content/krishiscan_export/labels.txt', 'w') as f:
    for name in class_names:
        f.write(name + '\n')

print('Exported files:')
print('/content/krishiscan_export/krishiscan_model.tflite')
print('/content/krishiscan_export/labels.txt')
```

---

## 9. Save to Google Drive

```python
import shutil, os

drive_out = '/content/drive/MyDrive/KrishiScanModel'
os.makedirs(drive_out, exist_ok=True)

shutil.copy('/content/krishiscan_export/krishiscan_model.tflite', drive_out + '/krishiscan_model.tflite')
shutil.copy('/content/krishiscan_export/labels.txt', drive_out + '/labels.txt')

print('Saved to:', drive_out)
```

---

## 10. Integrate into Android App
Copy files into:
- `mobile-android/app/src/main/assets/krishiscan_model.tflite`
- `mobile-android/app/src/main/assets/labels.txt`

Then rebuild app.

---

## 11. Free-Tier Tips (Important)
- Keep `BATCH_SIZE=16` (or 8 if OOM)
- Keep epochs low first (6-8)
- Use only selected 15 classes
- Save checkpoints to Drive often
- If session disconnects, remount Drive and resume from saved model

---

## 12. Troubleshooting

### Error: folder not found
Dataset folder names vary. Print folder tree:

```python
import os
for root, dirs, files in os.walk('/content/dataset'):
    if 'train' in root.lower() or 'valid' in root.lower():
        print(root)
```

### Error: output classes mismatch
`labels.txt` must be generated from `train_ds.class_names` (exact order), not manual typing.

### Poor prediction quality
- Increase training epochs slightly
- Fine-tune last 30 layers
- Ensure class balance and clean subset mapping

---

## 13. Deliverables Checklist
- [ ] `krishiscan_model.tflite` created
- [ ] `labels.txt` created
- [ ] both copied to Android `assets/`
- [ ] app inference runs without "Model not found"
- [ ] predictions map to expected class labels

---

If you want, next step I can generate a **single Colab notebook file (`.ipynb`)** in this repo with these exact cells ready to run.
