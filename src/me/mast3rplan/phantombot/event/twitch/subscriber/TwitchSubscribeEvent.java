/* 
 * Copyright (C) 2015 www.phantombot.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.mast3rplan.phantombot.event.twitch.subscriber;

import me.mast3rplan.phantombot.jerklib.Channel;

public class TwitchSubscribeEvent extends TwitchSubscriberEvent
{

    /**
     * 
     * @param subscriber 
     * 
     * @deprecated Use a version which accepts the channel argument instead
     */
    public TwitchSubscribeEvent(String subscriber)
    {
        super(subscriber, Type.SUBSCRIBE);
    }

    public TwitchSubscribeEvent(String subscriber, Channel channel)
    {
        super(subscriber, Type.SUBSCRIBE, channel);
    }
}
