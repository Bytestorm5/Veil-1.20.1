package foundry.veil.backport.blaze3d.vertex;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * Backport of {@code com.mojang.blaze3d.vertex.ByteBufferBuilder}: a growable off-heap buffer that hands out results
 * which stay valid until the builder is cleared, discarded or closed.
 */
public class ByteBufferBuilder implements AutoCloseable {

    private static final int MAX_GROWTH_SIZE = 2097152;

    private long pointer;
    private int capacity;
    private int writeOffset;
    private int nextResultOffset;
    private int resultCount;
    private int generation;

    public ByteBufferBuilder(int capacity) {
        this.capacity = capacity;
        this.pointer = MemoryUtil.nmemAlloc(capacity);
        if (this.pointer == MemoryUtil.NULL) {
            throw new OutOfMemoryError("Failed to allocate " + capacity + " bytes");
        }
    }

    /**
     * Reserves the specified number of bytes at the end of the buffer.
     *
     * @param bytes The number of bytes to reserve
     * @return A pointer to the start of the reserved memory
     */
    public long reserve(int bytes) {
        int offset = this.writeOffset;
        int end = offset + bytes;
        this.ensureCapacity(end);
        this.writeOffset = end;
        return this.pointer + offset;
    }

    private void ensureCapacity(int size) {
        if (size > this.capacity) {
            int growth = Math.min(this.capacity, MAX_GROWTH_SIZE);
            int newCapacity = Math.max(this.capacity + growth, size);
            this.resize(newCapacity);
        }
    }

    private void resize(int newCapacity) {
        long newPointer = MemoryUtil.nmemRealloc(this.pointer, newCapacity);
        if (newPointer == MemoryUtil.NULL) {
            throw new OutOfMemoryError("Failed to resize buffer from " + this.capacity + " bytes to " + newCapacity + " bytes");
        }
        this.pointer = newPointer;
        this.capacity = newCapacity;
    }

    /**
     * @return All bytes written since the last result, or <code>null</code> if nothing was written
     */
    public @Nullable Result build() {
        this.checkOpen();
        int offset = this.nextResultOffset;
        int size = this.writeOffset - offset;
        if (size == 0) {
            return null;
        }
        this.nextResultOffset = this.writeOffset;
        this.resultCount++;
        return new Result(offset, size, this.generation);
    }

    public void clear() {
        if (this.resultCount > 0) {
            throw new IllegalStateException("Tried to clear buffer while results are still in use");
        }
        this.discard();
    }

    public void discard() {
        this.checkOpen();
        if (this.resultCount > 0) {
            this.discardResults();
        }
        this.writeOffset = 0;
        this.nextResultOffset = 0;
    }

    private void discardResults() {
        this.resultCount = 0;
        this.generation++;
    }

    boolean isValid(int generation) {
        return generation == this.generation;
    }

    void freeResult() {
        if (--this.resultCount <= 0) {
            this.resultCount = 0;
            this.writeOffset = 0;
            this.nextResultOffset = 0;
            this.generation++;
        }
    }

    private void checkOpen() {
        if (this.pointer == MemoryUtil.NULL) {
            throw new IllegalStateException("Buffer has been freed");
        }
    }

    @Override
    public void close() {
        if (this.pointer != MemoryUtil.NULL) {
            MemoryUtil.nmemFree(this.pointer);
            this.pointer = MemoryUtil.NULL;
            this.generation = -1;
        }
    }

    public class Result implements AutoCloseable {

        private final int offset;
        private final int capacity;
        private final int generation;
        private boolean closed;

        Result(int offset, int capacity, int generation) {
            this.offset = offset;
            this.capacity = capacity;
            this.generation = generation;
        }

        public ByteBuffer byteBuffer() {
            if (!ByteBufferBuilder.this.isValid(this.generation)) {
                throw new IllegalStateException("Buffer is no longer valid");
            }
            return MemoryUtil.memByteBuffer(ByteBufferBuilder.this.pointer + this.offset, this.capacity);
        }

        @Override
        public void close() {
            if (!this.closed) {
                this.closed = true;
                if (ByteBufferBuilder.this.isValid(this.generation)) {
                    ByteBufferBuilder.this.freeResult();
                }
            }
        }
    }
}
