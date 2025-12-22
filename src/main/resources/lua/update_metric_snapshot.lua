-- Lua script for atomically updating metric snapshot cache
-- Args: KEYS[1] = cache key, ARGV[1] = new value (a), ARGV[2] = new timestamp (ts), ARGV[3] = new source type (s), ARGV[4] = max history depth, ARGV[5] = timeout in seconds
local key = KEYS[1]
local newValue = ARGV[1]
local newTs = tonumber(ARGV[2])
local newSourceType = tonumber(ARGV[3])
local maxHistoryDepth = tonumber(ARGV[4])
local timeoutSeconds = tonumber(ARGV[5])

-- Get existing value
local existingJson = redis.call('GET', key)

-- cjson is available as a global variable in Redis Lua environment
local existing = nil
local history = {}

-- Parse existing JSON if exists
if existingJson and existingJson ~= '' then
    local success, result = pcall(function() return cjson.decode(existingJson) end)
    if success and result then
        existing = result
        -- Extract existing history if present
        if existing.h and type(existing.h) == 'table' then
            history = existing.h
        end
        -- Add current value to history if a and ts are present
        if existing.a and existing.ts then
            local previousValue = {
                a = existing.a,
                ts = existing.ts,
                s = existing.s
            }
            -- Insert at the beginning of history
            table.insert(history, 1, previousValue)
        end
    end
end

-- Limit history depth
while #history > maxHistoryDepth do
    table.remove(history, 1)
end

-- Build new JSON
local newData = {
    a = newValue,
    ts = newTs,
    s = newSourceType,
    h = history
}

local newJson = cjson.encode(newData)

-- Set new value
redis.call('SET', key, newJson)

-- Set expiration
if timeoutSeconds and timeoutSeconds > 0 then
    redis.call('EXPIRE', key, timeoutSeconds)
end

return newJson

