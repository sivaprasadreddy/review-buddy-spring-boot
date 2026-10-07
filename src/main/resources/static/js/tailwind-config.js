// Tailwind Play CDN config: Review Buddy theme (light green, white, black).
tailwind.config = {
  theme: {
    extend: {
      colors: {
        green: { DEFAULT: "#b9e2a8", soft: "#e9f6e2", dark: "#a6d894" },
        line: "#dfe6e4",
        grey: "#5a5c55",
      },
      fontFamily: {
        sans: ['"DM Sans"', "system-ui", "sans-serif"],
        heading: ["Fraunces", "Georgia", "serif"],
      },
    },
  },
};
