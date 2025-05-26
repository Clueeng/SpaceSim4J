#version 120

uniform vec2 u_resolution;
uniform float u_time;
uniform vec2 u_camera;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 6; i++) {
        value += amplitude * noise(p);
        p *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}

float spiralArm(vec2 p, float armOffset) {
    float angle = atan(p.y, p.x) + armOffset;
    float radius = length(p);

    // Create spiral shape
    float spiral = angle + log(radius + 0.1) * 2.0 + u_time * 0.02;

    // Make the spiral repeat
    spiral = sin(spiral) * 0.5 + 0.5;

    // Fade with distance from center
    float fade = exp(-radius * 1.5);

    return spiral * fade;
}

float galacticCore(vec2 p) {
    float dist = length(p);
    float core = exp(-dist * 8.0) * 2.0;

    // Add some noise for texture
    core += fbm(p * 10.0) * 0.3 * exp(-dist * 4.0);

    return core;
}

float dustLanes(vec2 p) {
    float angle = atan(p.y, p.x);
    float radius = length(p);

    // Create dust lane pattern
    float dust = sin(angle * 8.0 + radius * 15.0 + u_time * 0.01) * 0.5 + 0.5;
    dust = pow(dust, 3.0);

    // Fade with distance
    dust *= exp(-radius * 2.0);

    return dust;
}

vec3 starField(vec2 uv) {
    vec3 stars = vec3(0.0);

    // Multiple layers of stars at different scales
    for (int i = 0; i < 3; i++) {
        float scale = 50.0 + float(i) * 100.0;
        vec2 grid = fract(uv * scale);
        vec2 id = floor(uv * scale);

        float starChance = 0.98 + float(i) * 0.005;
        float star = step(starChance, hash(id));

        // Different star sizes
        float starSize = 0.02 / (1.0 + float(i));
        float brightness = smoothstep(starSize, 0.0, length(grid - 0.5));

        // Star colors based on layer
        vec3 starColor = vec3(1.0, 0.9, 0.7);
        if (i == 1) starColor = vec3(0.8, 0.9, 1.0);
        if (i == 2) starColor = vec3(1.0, 0.8, 0.6);

        stars += starColor * star * brightness * (0.8 + 0.4 * float(i));
    }

    return stars;
}

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;
    vec2 centered = (uv - 0.5) * 2.0;
    centered.x *= u_resolution.x / u_resolution.y;

    // Apply camera offset
    vec2 cameraOffset = u_camera * 0.0001;
    centered += cameraOffset;

    // Scale for galaxy size
    vec2 galaxyUV = centered * 0.8;

    // Create spiral arms
    float arm1 = spiralArm(galaxyUV, 0.0);
    float arm2 = spiralArm(galaxyUV, 2.094); // 120 degrees
    float arm3 = spiralArm(galaxyUV, 4.188); // 240 degrees

    float spirals = max(max(arm1, arm2), arm3);
    spirals = pow(spirals, 1.5);

    // Galactic center
    float core = galacticCore(galaxyUV);

    // Dust lanes
    float dust = dustLanes(galaxyUV);

    // Background nebula with more variation
    float nebula = fbm(galaxyUV * 3.0 + vec2(u_time * 0.01, 0.0));
    nebula += fbm(galaxyUV * 8.0 - vec2(0.0, u_time * 0.02)) * 0.5;
    nebula = pow(nebula * 0.7, 2.0);

    // Galaxy colors
    vec3 spiralColor = vec3(0.4, 0.6, 1.0) * spirals;
    vec3 coreColor = vec3(1.0, 0.8, 0.4) * core;
    vec3 dustColor = vec3(0.2, 0.1, 0.05) * dust;
    vec3 nebulaColor = vec3(0.15, 0.1, 0.3) * nebula;

    // Combine galaxy elements
    vec3 galaxy = spiralColor + coreColor - dustColor * 0.5 + nebulaColor;

    // Add stars
    vec3 stars = starField(uv + cameraOffset);

    // Background gradient
    float distFromCenter = length(centered);
    vec3 background = vec3(0.02, 0.01, 0.05) * (1.0 - distFromCenter * 0.3);

    // Final composition
    vec3 finalColor = background + galaxy + stars;

    // Add subtle color variation across the field
    finalColor += vec3(0.05, 0.03, 0.08) * sin(uv.x * 6.28 + u_time * 0.1);

    // Tone mapping for better contrast
    finalColor = finalColor / (finalColor + 1.0);

    gl_FragColor = vec4(finalColor, 1.0);
}