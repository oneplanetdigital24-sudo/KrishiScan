export type AuthContext = {
  uid: string;
  email?: string;
};

export type AuthedRequest<TBody = unknown, TQuery = unknown, TParams = unknown> = {
  user: AuthContext;
  body: TBody;
  query: TQuery;
  params: TParams;
};
