package com.github.martinambrus.rdforward.world;

import java.util.Random;

/**
 * Ken Perlin's improved noise function (2002).
 *
 * Produces smooth, continuous pseudorandom values in the range [-1, 1]
 * for any 3D coordinate. Used by terrain generators for heightmaps,
 * cave density fields, ore distribution, and biome blending.
 *
 * Thread-safe: the permutation table is initialized once at construction
 * and never modified, so multiple threads can call {@link #noise} and
 * {@link #octaveNoise} concurrently.
 */
public final class PerlinNoise {

    private final int[] perm;

    /**
     * Create a noise generator with the given seed.
     * Different seeds produce different but equally smooth noise patterns.
     */
    public PerlinNoise(long seed) {
        perm = new int[512];
        int[] base = new int[256];
        for (int i = 0; i < 256; i++) {
            base[i] = i;
        }
        // Fisher-Yates shuffle seeded by the world seed
        Random rng = new Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = base[i];
            base[i] = base[j];
            base[j] = tmp;
        }
        // Double the table to avoid index wrapping
        for (int i = 0; i < 256; i++) {
            perm[i] = base[i];
            perm[i + 256] = base[i];
        }
    }

    /**
     * Evaluate 3D Perlin noise at the given coordinates.
     *
     * @return a value in the range [-1, 1]
     */
    public double noise(double x, double y, double z) {
        // Find unit cube containing the point
        int xi = floor(x) & 255;
        int yi = floor(y) & 255;
        int zi = floor(z) & 255;

        // Relative position within the cube
        double xf = x - floor(x);
        double yf = y - floor(y);
        double zf = z - floor(z);

        // Fade curves for smooth interpolation
        double u = fade(xf);
        double v = fade(yf);
        double w = fade(zf);

        // Hash the 8 cube corners
        int a  = perm[xi] + yi;
        int aa = perm[a] + zi;
        int ab = perm[a + 1] + zi;
        int b  = perm[xi + 1] + yi;
        int ba = perm[b] + zi;
        int bb = perm[b + 1] + zi;

        // Trilinear interpolation of gradient dot products
        return lerp(w,
            lerp(v,
                lerp(u, grad(perm[aa], xf, yf, zf),
                        grad(perm[ba], xf - 1, yf, zf)),
                lerp(u, grad(perm[ab], xf, yf - 1, zf),
                        grad(perm[bb], xf - 1, yf - 1, zf))),
            lerp(v,
                lerp(u, grad(perm[aa + 1], xf, yf, zf - 1),
                        grad(perm[ba + 1], xf - 1, yf, zf - 1)),
                lerp(u, grad(perm[ab + 1], xf, yf - 1, zf - 1),
                        grad(perm[bb + 1], xf - 1, yf - 1, zf - 1))));
    }

    /**
     * Evaluate 2D Perlin noise (z = 0).
     */
    public double noise(double x, double y) {
        return noise(x, y, 0.0);
    }

    /**
     * Fractal Brownian Motion — layer multiple octaves of noise for
     * natural-looking terrain with both large-scale hills and fine detail.
     *
     * @param x           world X coordinate
     * @param y           world Y (or Z) coordinate
     * @param z           world Z coordinate
     * @param octaves     number of noise layers (typically 4-8)
     * @param persistence amplitude multiplier per octave (typically 0.5)
     * @return combined noise value (range depends on octaves/persistence)
     */
    public double octaveNoise(double x, double y, double z, int octaves, double persistence) {
        double total = 0.0;
        double frequency = 1.0;
        double amplitude = 1.0;
        double maxAmplitude = 0.0;

        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency, z * frequency) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= persistence;
            frequency *= 2.0;
        }

        // Normalize to [-1, 1]
        return total / maxAmplitude;
    }

    /**
     * 2D fractal noise (convenience overload).
     */
    public double octaveNoise(double x, double y, int octaves, double persistence) {
        return octaveNoise(x, y, 0.0, octaves, persistence);
    }

    // -- Perlin internals --

    private static double fade(double t) {
        // 6t^5 - 15t^4 + 10t^3
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        // Use the low 4 bits to select one of 12 gradient directions
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private static int floor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }
}
