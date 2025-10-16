import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import Home from '../views/Home.vue';
import PopularTracks from '../views/PopularTracks.vue';
import PopularAlbums from '../views/PopularAlbums.vue';

const routes: Array<RouteRecordRaw> = [
  { path: '/', name: 'Home', component: Home },
  { path: '/tracks/popular', name: 'tracks-popular', component: PopularTracks, meta: { title: 'Popular Tracks', type: 'tracks' } },
  { path: '/albums/popular', name: 'albums-popular', component: PopularAlbums, meta: { title: 'Popular Albums', type: 'albums' } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
