--[[

    Copyright (C) 2017-2018 ZTE, Inc. and others. All rights reserved. (ZTE)

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
local _M = {}
_M._VERSION = '1.0.0'
_M._DESCRIPTION = 'config_default'

local str_util  =  require('lib.utils.str_util')
local mark_empty_as_nil = str_util.mark_empty_as_nil
_M.plugins_default = {
	{["name"] = "redirect-transformer",["status"] = mark_empty_as_nil(os.getenv("MSB_REDIRECT_TRANSFORMER_PLUGIN")) or "on"}
}

return _M
