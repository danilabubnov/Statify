export type POPULAR_CONTENT = 'albums' | 'tracks' | 'artists'

declare module 'vue-router' {
  interface RouteMeta {
    title: string
    type: POPULAR_CONTENT
  }
}
