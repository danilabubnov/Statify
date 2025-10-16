import 'dotenv/config';
import type { CodegenConfig } from '@graphql-codegen/cli';

const api = process.env.VITE_STATIFY_DATA_API_URL;

const config: CodegenConfig = {
  schema: ['../statify-data-api/src/main/resources/**/*.graphqls', ...(api ? [{ [`${api.replace(/\/+$/, '')}/graphql`]: {} } as any] : [])],

  documents: ['src/**/*.{ts,tsx,vue,gql,graphql}'],

  generates: {
    'src/graphql/generated/graphql.ts': {
      plugins: ['typescript', 'typescript-operations', 'typed-document-node'],
      config: {
        gqlTagName: 'gql',

        useTypeImports: true,
        enumsAsTypes: true,
        skipTypename: false,
        dedupeFragments: true,

        defaultScalarType: 'unknown',
        scalars: { ID: 'string', Long: 'number' },
      },
    },
  },

  hooks: {
    afterOneFileWrite: ['prettier --loglevel silent --write'],
  },
};

export default config;

// npx graphql-codegen
