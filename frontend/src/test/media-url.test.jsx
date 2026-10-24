import { afterEach, expect, it } from 'vitest';
import { media } from '../components/CommerceUI';
import { apiClient } from '../services/apiClient';
const original = apiClient.defaults.baseURL;
afterEach(() => { apiClient.defaults.baseURL = original; });
it('resolves uploaded media with the same-origin production API', () => {
  apiClient.defaults.baseURL = '/api';
  expect(media('/media/example.png')).toBe(`${window.location.origin}/media/example.png`);
});
it('preserves local API origin and external product images', () => {
  apiClient.defaults.baseURL = 'http://localhost:8080/api';
  expect(media('/media/example.png')).toBe('http://localhost:8080/media/example.png');
  expect(media('https://example.com/product.jpg')).toBe('https://example.com/product.jpg');
});
