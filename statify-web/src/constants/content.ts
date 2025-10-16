export enum ContentType {
  TRACKS = 'tracks',
  ALBUMS = 'albums',
  ARTISTS = 'artists'
}

export const CONTENT_TITLES: Record<string, string> = {
  [ContentType.TRACKS]: 'Top tracks right now',
  [ContentType.ALBUMS]: 'Top albums right now',
  [ContentType.ARTISTS]: 'Top artists right now'
};
