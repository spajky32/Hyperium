/*
 *     Copyright (C) 2018  Hyperium <https://hyperium.cc/>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cc.hyperium.handlers.handlers;

import cc.hyperium.Hyperium;
import cc.hyperium.mods.sk1ercommon.Multithreading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Sk1er
 */
public class CommandQueue {

    private final long DELAY = 1000;
    private ConcurrentLinkedQueue<String> commands = new ConcurrentLinkedQueue<>();
    private long last = System.currentTimeMillis();
    private ConcurrentHashMap<String, Runnable> asyncCallbacks = new ConcurrentHashMap<>();

    public CommandQueue() {
        Multithreading.schedule(CommandQueue.this::check, 0, DELAY, TimeUnit.MILLISECONDS);
    }

    public void register(String chat, Runnable task) {
        System.out.println("Queued callback for " + chat);
        asyncCallbacks.put(chat, task);
        queue(chat);
    }

    private void check() {
        if (!commands.isEmpty()) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            if (thePlayer != null) {
                String poll = commands.poll();
                Hyperium.LOGGER.info("Sending chat: " + poll);
                Runnable runnable = asyncCallbacks.get(poll);
                thePlayer.sendChatMessage(poll);
                if (runnable != null) {
                    System.out.println("Running runnable for " + poll);
                    runnable.run();
                }

            }
        }
        last = System.currentTimeMillis();
    }

    public void queue(String message) {
        System.out.println("queued command: " + message);
        commands.add(message);
    }
}
