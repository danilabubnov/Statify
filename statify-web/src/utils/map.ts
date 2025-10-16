export function mapFirstValue<K, V>(m: Map<K, V>): V | undefined {
  return m.values().next().value;
}