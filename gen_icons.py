import struct, zlib, os, math

def create_png(width, height, pixels):
    """pixels: list of (r,g,b,a) tuples, row by row"""
    def chunk(chunk_type, data):
        c = chunk_type + data
        crc = struct.pack('>I', zlib.crc32(c) & 0xFFFFFFFF)
        return struct.pack('>I', len(data)) + c + crc
    
    # PNG signature
    sig = b'\x89PNG\r\n\x1a\n'
    
    # IHDR
    ihdr = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    
    # Raw image data with filter byte (0 = None) per row
    raw = b''
    for y in range(height):
        raw += b'\x00'  # filter byte
        for x in range(width):
            r, g, b, a = pixels[y * width + x]
            raw += struct.pack('BBBB', r, g, b, a)
    
    # IDAT
    compressed = zlib.compress(raw)
    
    return sig + chunk(b'IHDR', ihdr) + chunk(b'IDAT', compressed) + chunk(b'IEND', b'')

def make_icon(size):
    """Generate a deep-blue circle with neon pink play triangle"""
    pixels = []
    cx, cy = size / 2, size / 2
    margin = size * 0.09
    radius = size / 2 - margin
    
    tri_h = size * 0.35  # triangle half-width/height scale
    
    for y in range(size):
        for x in range(size):
            dx, dy = x - cx, y - cy
            dist = math.sqrt(dx*dx + dy*dy)
            
            if dist <= radius:
                # Inside circle - dark blue background
                # Slight radial gradient effect
                t = dist / radius
                r = int(10 + t * 12)
                g = int(10 + t * 8)
                b_val = int(26 + t * 20)
                
                # Check if inside play triangle
                # Triangle vertices: top-left offset from center
                tx1, ty1 = cx - tri_h * 0.5, cy - tri_h * 0.55
                tx2, ty2 = cx + tri_h * 0.55, cy
                tx3, ty3 = cx - tri_h * 0.5, cy + tri_h * 0.55
                
                # Barycentric check
                def sign(p1x, p1y, p2x, p2y, p3x, p3y):
                    return (p1x - p3x) * (p2y - p3y) - (p2x - p3x) * (p1y - p3y)
                
                d1 = sign(x, y, tx1, ty1, tx2, ty2)
                d2 = sign(x, y, tx2, ty2, tx3, ty3)
                d3 = sign(x, y, tx3, ty3, tx1, ty1)
                
                has_neg = (d1 < 0) or (d2 < 0) or (d3 < 0)
                has_pos = (d1 > 0) or (d2 > 0) or (d3 > 0)
                
                if not (has_neg and has_pos):
                    # Inside triangle - neon pink
                    # Edge glow effect
                    edge_dist = min(abs(d1), abs(d2), abs(d3))
                    if edge_dist < 3 and size > 48:
                        pixels.append((255, 107, 129, 255))  # glow
                    else:
                        pixels.append((233, 69, 96, 255))  # core pink
                else:
                    pixels.append((r, g, b_val, 255))  # background
            else:
                pixels.append((0, 0, 0, 0))  # transparent
    
    return create_png(size, size, pixels)

# Generate all densities
out_dir = r'C:\Users\1\WorkBuddy\2026-06-01-08-35-18\VideoPlayer\app\src\main\res'
sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192,
}

for folder, size in sizes.items():
    path = os.path.join(out_dir, folder)
    os.makedirs(path, exist_ok=True)
    
    png_data = make_icon(size)
    
    fname = os.path.join(path, 'ic_launcher.png')
    with open(fname, 'wb') as f:
        f.write(png_data)
    print(f'{fname} ({size}x{size}) - {len(png_data)} bytes')
    
    # Round icon (same for now - modern launchers use adaptive icon XML anyway)
    fname_round = os.path.join(path, 'ic_launcher_round.png')
    with open(fname_round, 'wb') as f:
        f.write(png_data)
    print(f'{fname_round} ({size}x{size}) - {len(png_data)} bytes')

print('\nAll icons generated!')
