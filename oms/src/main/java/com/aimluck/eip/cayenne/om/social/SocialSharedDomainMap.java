/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
 * http://www.aipo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aimluck.eip.cayenne.om.social;

import com.aimluck.eip.cayenne.om.social.auto._SocialSharedDomainMap;

public class SocialSharedDomainMap extends _SocialSharedDomainMap {

    private static SocialSharedDomainMap instance;

    private SocialSharedDomainMap() {}

    public static SocialSharedDomainMap getInstance() {
        if(instance == null) {
            instance = new SocialSharedDomainMap();
        }

        return instance;
    }
}
