--[[

    Copyright (C) 2017 ZTE, Inc. and others. All rights reserved. (ZTE)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--]]

local Object = require "vendor.classic"
local BasePlugin = Object:extend()

function BasePlugin:new(name)
	self._name = name
	ngx.log(ngx.DEBUG, "executing plugin \""..self._name.."\": new") 
end

function BasePlugin:access()
	ngx.log(ngx.DEBUG, "executing plugin \""..self._name.."\": access")
end

function BasePlugin:header_filter()
  ngx.log(ngx.DEBUG, " executing plugin \""..self._name.."\": header_filter")
end

return BasePlugin