q#!/usr/bin/env python3
import re

with open('/Users/admin/Documents/PullChord-Report/Scadda-Report/src/main/resources/templates/KD_VECV_NewClientDemoUI.html', 'r') as f:
    content = f.read()

# 1. Update CSS for Exact Repliaca
header_css = '''/* Reference Header Style */
        .top-header { 
            height: 55px; 
            background: #001529; /* Deep Navy */
            border-bottom: 3px solid #ffcc00; 
            display: flex; 
            align-items: center; 
            justify-content: space-between; 
            padding: 0 20px; 
            color: white; 
            font-family: 'Segoe UI', sans-serif;
            flex-shrink: 0;
            z-index: 100;
        }

        .header-left { display: flex; align-items: center; gap: 15px; }
        .header-title { font-size: 18px; font-weight: 700; letter-spacing: 0.5px; }
        
        .header-right { display: flex; align-items: center; gap: 20px; font-size: 13px; font-weight: 500; }
        
        /* Live Indicator */
        .live-indicator { display: flex; align-items: center; gap: 6px; }
        .status-dot { width: 10px; height: 10px; background-color: #ff0000; border-radius: 50%; box-shadow: 0 0 5px #ff0000; animation: blink 1.5s infinite; }
        @keyframes blink { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }
        
        /* Toggle Switch */
        .theme-switch { position: relative; width: 40px; height: 20px; background: #555; border-radius: 20px; cursor: pointer; transition: 0.3s; display:flex; align-items:center; padding: 2px; }
        .theme-switch.active { background: #555; } /* Keep same for now or change */
        .switch-knob { width: 18px; height: 18px; background: white; border-radius: 50%; transition: 0.3s; display:flex; align-items:center; justify-content:center; color: #f59e0b; font-size: 10px; transform: translateX(0); }
        [data-theme="light"] .switch-knob { transform: translateX(20px); }
'''

# Inject new CSS (replace previous header css block or append)
# I'll replace the previous .top-header CSS block
content = re.sub(r'\.top-header \{.*?\}.*?\.theme-btn:hover \{.*?\}', header_css, content, flags=re.DOTALL)

# 2. Update HTML Content
# Header Structure: [Logo | VECV Dashboard] ... [Toggle | Red Dot LIVE | Date Time]
header_html = '''<!-- Top Header (Reference Match) -->
        <div class="top-header">
            <!-- Left -->
            <div class="header-left">
                <img src="/new_loho_VECV-removebg-preview.png" style="height:35px;">
                <div class="header-title">VECV Dashboard</div>
            </div>
            
            <!-- Right -->
            <div class="header-right">
                <!-- Theme Toggle -->
                <div class="theme-switch" onclick="toggleTheme()" title="Toggle Theme">
                    <div class="switch-knob"><i class="fas fa-sun"></i></div>
                </div>
                
                <!-- Live Indicator -->
                <div class="live-indicator">
                    <div class="status-dot"></div>
                    <span>LIVE</span>
                </div>
                
                <!-- Clock -->
                <div id="clock">--/--/---- --:--:--</div>
            </div>
        </div>

        <script>
            function toggleTheme() {
                const body = document.body;
                const current = body.getAttribute('data-theme');
                body.setAttribute('data-theme', current === 'light' ? 'dark' : 'light');
            }
        </script>'''

# Replace HTML
content = re.sub(r'<div class="top-header">.*?<\/div>\s*<\/div>', header_html, content, flags=re.DOTALL)

# Remove the old clock script to avoid duplicates (if I inserted one in HTML above, I don't need the old one)
# I'll just update the old clock script to format match "29-11-2025 15:33:01"
clock_script = '''
        // Real-time Clock (DD-MM-YYYY HH:mm:ss)
        setInterval(() => {
            const now = new Date();
            const day = String(now.getDate()).padStart(2, '0');
            const month = String(now.getMonth() + 1).padStart(2, '0');
            const year = now.getFullYear();
            const time = now.toLocaleTimeString('en-GB', { hour12: false }); // 24h format
            document.getElementById('clock').innerText = `${day}-${month}-${year} ${time}`;
        }, 1000);
'''
# Replace old clock script
content = re.sub(r'\/\/ Real-time Clock.*?\}, 1000\);', clock_script, content, flags=re.DOTALL)


with open('/Users/admin/Documents/PullChord-Report/Scadda-Report/src/main/resources/templates/KD_VECV_NewClientDemoUI.html', 'w') as f:
    f.write(content)

print("✅ Applied exact header replica: Navy Blue, Yellow Strip, Logo, Live Indicator, Toggle")
