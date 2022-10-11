local storedPermits = 'storedPermits'
local maxPermits = 'maxPermits'
local stableIntervalMicros = 'stableIntervalMicros'
local nextFreeTicketMicros = 'nextFreeTicketMicros'

local function reserveAndGetWaitLength(key, permits, nowMicros)
    local limiterInfo = redis.call('HMGET', key, storedPermits, maxPermits, stableIntervalMicros, nextFreeTicketMicros)
--     2  199 0
    local stored = tonumber(limiterInfo[1])
--     2  200 200
    local max = tonumber(limiterInfo[2])
--     1000 / 2
    local interval = tonumber(limiterInfo[3])
--     0 1641392377000 1641393700000
    local nextMicros = tonumber(limiterInfo[4])
--     1641392377000 >= 0 1641392900000 >= 1641392377000 1641393700000 > 1641392900000
    if (nowMicros >= nextMicros)
--     当前时间晚于nextFreeTicketMicros，则计算该段时间内可以生成多少令牌，将生成的令牌加入令牌桶中并更新数据
    then
        local newPermits = (nowMicros - nextMicros) / interval
--         取这段时间内可产生的令牌，有可能比最大令牌少
--      200  100
        stored = math.min(max, newPermits)
        nextMicros = nowMicros
    end
-- 本次可拿取的令牌 1 min(100,500)
    local storedToSpend = math.min(stored, permits)
-- 需要等的令牌数0或者是大于0 0 0 400
    local freshPermits = permits - storedToSpend
-- 等待的长度 0 0 400 *
    local waitMicros = freshPermits * interval
--     增加令牌 1641392377000 1641392900000  1641393900000 + 400 *
    local newNextMicros = nextMicros + waitMicros
    stored = stored - storedToSpend
--   199  0   newNextMicros  修改为--->  nextMicros，确保多者进来后抛弃掉，不修改时间，避免等待叠加
    redis.call('HMSET', key, storedPermits, stored, nextFreeTicketMicros, newNextMicros)
    return newNextMicros
end

local function acquire(key, permits, nowMicros)
    local wait = reserveAndGetWaitLength(key, permits, nowMicros)
    return math.max(wait - nowMicros, 0)
end

local function tryAcquire(key, permits, nowMicros, timeoutMicros)
    local next = tonumber(redis.call('HGET', key, nextFreeTicketMicros))
    if (nowMicros + timeoutMicros <= next)
    then
        -- tryAcquire false
        return -1
    else
        local wait = reserveAndGetWaitLength(key, permits, nowMicros)
        return math.max(wait - nowMicros, 0)
    end
end

local key = KEYS[1]
local method = ARGV[1]
if method == 'acquire' then
    return acquire(key, tonumber(ARGV[2]), tonumber(ARGV[3]))
elseif method == 'tryAcquire' then
    return tryAcquire(key, tonumber(ARGV[2]), tonumber(ARGV[3]), tonumber(ARGV[4]))
end