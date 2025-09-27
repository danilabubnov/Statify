import { ApolloClient, ApolloLink } from '@apollo/client';
import { ServerError } from '@apollo/client';
import { InMemoryCache } from '@apollo/client';
import { HttpLink } from '@apollo/client';
import { SetContextLink } from '@apollo/client/link/context';
import { ErrorLink } from '@apollo/client/link/error';
import { from, switchMap } from 'rxjs';

const httpLink = new HttpLink({
  uri: `${import.meta.env.VITE_STATIFY_DATA_API_URL}/graphql`,
});

const contextLink = new SetContextLink(async (prev, _) => {
  const { authStore } = await import('../store/auth/auth');
  const token = authStore().accessToken;

  return {
    headers: {
      ...(prev.headers || {}),
      Authorization: token ? `Bearer ${token}` : '',
    },
  };
});

let refreshPromise: Promise<string | null> | null = null;

function getRefresh(): Promise<string | null> {
  if (!refreshPromise) {
    refreshPromise = import('../store/auth/auth')
      .then(({ authStore }) => authStore().refresh())
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

const errorLink = new ErrorLink(({ error, operation, forward }) => {
  if (error && ServerError.is(error) && error.statusCode === 401) {
    if (!refreshPromise) {
      refreshPromise = getRefresh();
    }

    return from(refreshPromise).pipe(
      switchMap((newAccessToken) => {
        const prev = operation.getContext().headers ?? {};
        operation.setContext({
          headers: { ...prev, Authorization: `Bearer ${newAccessToken}` },
        });
        return forward(operation);
      }),
    );
  }
});

export const apolloClient = new ApolloClient({
  link: ApolloLink.from([errorLink, contextLink, httpLink]),
  cache: new InMemoryCache(),
});
