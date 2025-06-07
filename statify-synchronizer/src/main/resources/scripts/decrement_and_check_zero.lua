local key     = KEYS[1]
local delta   = tonumber(ARGV[1])
local current = tonumber(redis.call('GET', key) or '0')

if current > delta then
  redis.call('DECRBY', key, delta)
  return 0
elseif current == delta then
  redis.call('DEL', key)
  return 1
else
  return 0
end