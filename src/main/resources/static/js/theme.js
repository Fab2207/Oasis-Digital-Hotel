// Theme Management Script
(function () {
  // Default colors
  const defaultPrimary = "#047857";
  const defaultAccent = "#D4AF37";
  const defaultBackgroundDark = "#121212"; // Greenish dark background
  const defaultSurfaceDark = "#1D2E24"; // Slightly lighter surface

  // Get saved theme from localStorage
  const savedPrimary = localStorage.getItem("theme-primary") || defaultPrimary;
  const savedAccent = localStorage.getItem("theme-accent") || defaultAccent;
  const savedBorder = localStorage.getItem("theme-border") || "0.5rem"; // Default rounded

  // Apply CSS variables to root
  const root = document.documentElement;
  root.style.setProperty("--color-primary", savedPrimary);
  root.style.setProperty("--color-accent", savedAccent);
  root.style.setProperty("--border-radius", savedBorder);

  // Tailwind Config Injection (for runtime classes if needed, though CSS vars are better)
  // We can't easily change tailwind config at runtime without a build step or CDN script re-init.
  // So we rely on CSS variables mapped in the Tailwind config script in common.html

  // Apply border radius to common elements if needed (or rely on CSS var in Tailwind)

  // Helper to set theme
  window.setAppTheme = function (primary, accent, border) {
    localStorage.setItem("theme-primary", primary);
    localStorage.setItem("theme-accent", accent);
    localStorage.setItem("theme-border", border);

    root.style.setProperty("--color-primary", primary);
    root.style.setProperty("--color-accent", accent);
    root.style.setProperty("--border-radius", border);

    // Reload to ensure all components (like charts) update if they don't observe CSS vars
    // Or we could emit an event. For now, reload is safest for full consistency as requested.
    location.reload();
  };
})();
