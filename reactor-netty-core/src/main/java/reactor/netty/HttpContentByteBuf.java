package reactor.netty;

import io.netty.buffer.ByteBuf;

public class HttpContentByteBuf extends WrappedByteBuf {

	private final boolean isEndOfStream;

	HttpContentByteBuf(ByteBuf delegate, boolean isEndOfStream) {
		super(delegate);
		this.isEndOfStream = isEndOfStream;
	}

	public boolean isEndOfStream() {
		return isEndOfStream;
	}
}
