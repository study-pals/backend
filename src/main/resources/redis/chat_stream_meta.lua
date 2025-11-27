-- KEYS[1..N] = stream keys
-- ARGV[1] = id field name
-- ARGV[2] = type field name
-- argv[3] = sender field name
-- argv[4] = message field name

-- return type
-- [1] = stream key
-- [2] = stream length
-- [3] = oldest id
-- [4] = newest id
-- [5] = latest chat id
-- [6] = latest chat type
-- [7] = latest chat sender
-- [8] = latest chat message

local idField     = ARGV[1]
local typeField   = ARGV[2]
local senderField = ARGV[3]
local msgField    = ARGV[4]

local results = {}

for i = 1, #KEYS do
    local key = KEYS[i]

    -- 키가 없으면 length=0, 나머지는 false
    if redis.call('EXISTS', key) == 0 then
        table.insert(results, {key, 0, false, false, false, false, false, false})
    else
        local len = redis.call('XLEN', key)

        local first = redis.call('XRANGE', key, '-', '+', 'COUNT', 1)
        local last  = redis.call('XREVRANGE', key, '+', '-', 'COUNT', 1)

        local oldestId = false
        local newestId = false

        if first ~= nil and #first > 0 then
            oldestId = first[1][1]
        end
        if last ~= nil and #last > 0 then
            newestId = last[1][1]
        end

        local lastFields = nil
        if last ~= nil and #last > 0 then
            lastFields = last[1][2]
        end

        local chatId  = false
        local msgType = false
        local sender  = false
        local message = false

        if lastFields ~= nil then
            for j = 1, #lastFields, 2 do
                local field = lastFields[j]
                local value = lastFields[j + 1]

                if field == idField then
                    chatId = value
                elseif field == typeField then
                    msgType = value
                elseif field == senderField then
                    sender = value
                elseif field == msgField then
                    message = value
                end
            end
        end

        -- { streamKey, length, oldestId, newestId, chatId, type, sender, message }
        table.insert(results, {key, len, oldestId, newestId, chatId, msgType, sender, message})
    end
end

return results