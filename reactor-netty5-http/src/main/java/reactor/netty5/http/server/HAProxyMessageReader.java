/*
 * Copyright (c) 2019-2022 VMware, Inc. or its affiliates, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.netty5.http.server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import io.netty5.channel.Channel;
import io.netty5.channel.ChannelHandlerAdapter;
import io.netty5.channel.ChannelHandlerContext;
import io.netty.contrib.handler.codec.haproxy.HAProxyMessage;
import io.netty5.util.AttributeKey;
import reactor.netty5.transport.AddressUtils;
import reactor.util.annotation.Nullable;

/**
 * Consumes {@link io.netty.contrib.handler.codec.haproxy.HAProxyMessage}
 * and set it into channel attribute for later use.
 *
 * @author aftersss
 */
final class HAProxyMessageReader extends ChannelHandlerAdapter {

	private static final AttributeKey<InetSocketAddress> REMOTE_ADDRESS_FROM_PROXY_PROTOCOL =
			AttributeKey.valueOf("remoteAddressFromProxyProtocol");

	private static final boolean hasProxyProtocol;

	static {
		boolean proxyProtocolCheck = true;
		try {
			Class.forName("io.netty.contrib.handler.codec.haproxy.HAProxyMessageDecoder");
		}
		catch (ClassNotFoundException cnfe) {
			proxyProtocolCheck = false;
		}
		hasProxyProtocol = proxyProtocolCheck;
	}

	static boolean hasProxyProtocol() {
		return hasProxyProtocol;
	}

	@Nullable
	static SocketAddress resolveRemoteAddressFromProxyProtocol(Channel channel) {
		if (HAProxyMessageReader.hasProxyProtocol()) {
			return channel.attr(REMOTE_ADDRESS_FROM_PROXY_PROTOCOL).getAndSet(null);
		}

		return null;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HAProxyMessage) {
			try (HAProxyMessage proxyMessage = (HAProxyMessage) msg) {
				if (proxyMessage.sourceAddress() != null && proxyMessage.sourcePort() != 0) {
					InetSocketAddress remoteAddress = AddressUtils
							.createUnresolved(proxyMessage.sourceAddress(), proxyMessage.sourcePort());
					ctx.channel()
							.attr(REMOTE_ADDRESS_FROM_PROXY_PROTOCOL)
							.set(remoteAddress);
				}
			}

			ctx.channel()
			   .pipeline()
			   .remove(this);

			ctx.read();
		}
		else {
			super.channelRead(ctx, msg);
		}
	}
}