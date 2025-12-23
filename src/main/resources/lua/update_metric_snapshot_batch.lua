-- Lua script for atomically updating multiple metric snapshot cache entries
-- Args: 
--   KEYS[1..N] = cache keys
--   ARGV[1..N*5] = for each key: value, timestamp, sourceType, maxHistoryDepth, timeoutSeconds
--   The arguments are organized as: ARGV[1]=value1, ARGV[2]=ts1, ARGV[3]=sourceType1, ARGV[4]=maxHistoryDepth1, ARGV[5]=timeoutSeconds1, ARGV[6]=value2, ...
local keyCount = #KEYS
local results = {}

for i = 1, keyCount do
    local key = KEYS[i]
    local argBase = (i - 1) * 5 + 1
    local newValue = ARGV[argBase]
    local newTs = tonumber(ARGV[argBase + 1])
    local newSourceType = tonumber(ARGV[argBase + 2])
    local maxHistoryDepth = tonumber(ARGV[argBase + 3])
    local timeoutSeconds = tonumber(ARGV[argBase + 4])

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

    -- Limit history depth by removing the last element
    while #history > maxHistoryDepth do
        table.remove(history)
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

    table.insert(results, newJson)
end

return results

