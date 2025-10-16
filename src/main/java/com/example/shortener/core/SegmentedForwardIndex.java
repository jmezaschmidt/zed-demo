package com.example.shortener.core;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A forward index implemented as a segmented array. IDs are mapped to
 * two-level array coordinates (segment index and offset) based on a
 * configurable shift. Each segment holds a fixed number of references
 * corresponding to contiguous ID ranges. This design avoids large contiguous
 * allocations and provides constant-time resolution of IDs.
 */
public class SegmentedForwardIndex implements ForwardIndex {
    /**
     * The number of bits to shift for computing the segment index. A value of
     * 20 yields segments of size 1<<20 (≈1 million entries), balancing memory
     * footprint and reallocation frequency.
     */
    private final int segmentShift;
    private final long segmentSize;
    private final long segmentMask;

    /**
     * Backing storage: an array of segments. Each segment is an array of
     * String references. Segments are allocated lazily on demand.
     */
    private volatile String[][] segments;

    /**
     * Tracks the highest ID assigned so far. Updated after writes to ensure
     * visibility to readers.
     */
    private final AtomicLong highWatermark = new AtomicLong(-1L);

    /**
     * Lock for segment expansion. Reads are lock-free; writes only lock when
     * allocating a new segment.
     */
    private final Lock expandLock = new ReentrantLock();

    public SegmentedForwardIndex() {
        this(20);
    }

    public SegmentedForwardIndex(int segmentShift) {
        if (segmentShift <= 0 || segmentShift >= 30) {
            throw new IllegalArgumentException("segmentShift must be between 1 and 29");
        }
        this.segmentShift = segmentShift;
        this.segmentSize = 1L << segmentShift;
        this.segmentMask = segmentSize - 1;
        // Preallocate a small number of segment slots; actual segments allocated on demand
        this.segments = new String[1][];
    }

    @Override
    public void put(long id, String url) {
        if (id < 0) {
            throw new IllegalArgumentException("id must be non-negative");
        }
        int segIndex = (int) (id >>> segmentShift);
        int offset = (int) (id & segmentMask);
        ensureCapacity(segIndex);
        String[] segment = segments[segIndex];
        segment[offset] = url;
        // Publish the new high watermark after the URL is visible
        long current;
        do {
            current = highWatermark.get();
        } while (id > current && !highWatermark.compareAndSet(current, id));
    }

    private void ensureCapacity(int segIndex) {
        // Check if segments array needs to grow to accommodate segIndex
        String[][] local = segments;
        if (segIndex < local.length) {
            // Allocate segment if null
            if (local[segIndex] == null) {
                expandLock.lock();
                try {
                    if (segments[segIndex] == null) {
                        segments[segIndex] = new String[(int) segmentSize];
                    }
                } finally {
                    expandLock.unlock();
                }
            }
            return;
        }
        // Need to grow the segments array
        expandLock.lock();
        try {
            // Double-check after acquiring lock
            if (segIndex < segments.length) {
                if (segments[segIndex] == null) {
                    segments[segIndex] = new String[(int) segmentSize];
                }
                return;
            }
            int newLength = Math.max(segIndex + 1, segments.length * 2);
            segments = Arrays.copyOf(segments, newLength);
            // Allocate the new segment
            segments[segIndex] = new String[(int) segmentSize];
        } finally {
            expandLock.unlock();
        }
    }

    @Override
    public Optional<String> get(long id) {
        long hw = highWatermark.get();
        if (id < 0 || id > hw) {
            return Optional.empty();
        }
        int segIndex = (int) (id >>> segmentShift);
        int offset = (int) (id & segmentMask);
        String[][] local = segments;
        if (segIndex >= local.length) {
            return Optional.empty();
        }
        String[] segment = local[segIndex];
        if (segment == null) {
            return Optional.empty();
        }
        String url = segment[offset];
        return Optional.ofNullable(url);
    }

    @Override
    public long highWatermark() {
        return highWatermark.get();
    }
}