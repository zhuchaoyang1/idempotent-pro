local result
local value = ARGV[1]
local exSeconds = ARGV[2]

print(KEYS[1])
print(value)
print(exSeconds)

result = redis.call("SET", KEYS[1], value, "EX", exSeconds, "NX")

return result;