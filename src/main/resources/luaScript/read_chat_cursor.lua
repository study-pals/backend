-- avgv[1] = hashKey(room:{roomId}:reads)
-- keys = user id list by String for request

local result = {}
for i, userId in ipairs(KEYS) do
    local val = redis.call("HGET", ARGV[1], userId)
    if val then
        result[#result + 1] = userId
        result[#result + 1] = val
    end
end
return result