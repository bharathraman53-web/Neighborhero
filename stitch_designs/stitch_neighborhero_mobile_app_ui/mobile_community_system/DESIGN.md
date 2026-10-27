---
name: Mobile Community System
colors:
  surface: '#f7f9ff'
  surface-dim: '#d7dadf'
  surface-bright: '#f7f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f1f4f9'
  surface-container: '#ebeef3'
  surface-container-high: '#e5e8ee'
  surface-container-highest: '#e0e3e8'
  on-surface: '#181c20'
  on-surface-variant: '#414844'
  inverse-surface: '#2d3135'
  inverse-on-surface: '#eef1f6'
  outline: '#717973'
  outline-variant: '#c1c8c2'
  surface-tint: '#3f6653'
  primary: '#012d1d'
  on-primary: '#ffffff'
  primary-container: '#1b4332'
  on-primary-container: '#86af99'
  inverse-primary: '#a5d0b9'
  secondary: '#116c4a'
  on-secondary: '#ffffff'
  secondary-container: '#a1f4c8'
  on-secondary-container: '#1b724f'
  tertiary: '#401b1b'
  on-tertiary: '#ffffff'
  tertiary-container: '#5a302f'
  on-tertiary-container: '#d29895'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#c1ecd4'
  primary-fixed-dim: '#a5d0b9'
  on-primary-fixed: '#002114'
  on-primary-fixed-variant: '#274e3d'
  secondary-fixed: '#a1f4c8'
  secondary-fixed-dim: '#86d7ad'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffdad8'
  tertiary-fixed-dim: '#f5b7b4'
  on-tertiary-fixed: '#331111'
  on-tertiary-fixed-variant: '#673a39'
  background: '#f7f9ff'
  on-background: '#181c20'
  surface-variant: '#e0e3e8'
typography:
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system is built for a human-centered mobile application focused on everyday community engagement and local support. The brand personality is approachable, trusted, warm, and highly reliable. It evokes feelings of safety, neighborly connection, and quiet confidence.

We embrace a modern, clean, and tactile aesthetic inspired by Android Material 3 guidelines, combined with soft geometric typography and organic forest-green tones. Surfaces feel approachable and inviting, avoiding harsh contrasts in favor of friendly, content-forward layouts.

## Colors

The color palette grounds the mobile interface in trustworthy, organic tones. The deep forest-green primary (`#1B4332`) anchors key interactive elements and primary actions, while the subtle teal highlight (`#40916C`) draws attention to secondary actions and active states. 

The background is set to a warm off-white (`#F8F9FA`) to reduce eye strain and provide a soft canvas for cards and elevated components. Dark charcoal (`#212529`) is used for all primary typography to maintain high legibility without the harshness of pure black.

## Typography

Typography pairs the soft, rounded geometric forms of Plus Jakarta Sans for headlines with the highly legible, neutral utility of Inter for body text and labels. This combination creates an optimistic yet precise reading experience optimized for mobile screens.

Ensure that text scaling respects system accessibility settings. Avoid using headline styles larger than 28px on standard mobile viewports to prevent truncation and maintain balanced hierarchy within card-based layouts.

## Layout & Spacing

The layout relies on a fluid mobile grid system structured around 16px (1rem) horizontal outer margins and consistent 16px gutters between cards and stacked components. This ensures thumb-friendly touch targets and comfortable breathing room across various smartphone aspect ratios.

Spacing tokens follow a strict 4px/8px baseline grid. Use `space-xs` and `space-sm` for tight inline element pairings (such as icon-to-label gaps), `space-md` for standard component padding, and `space-lg` to `space-xl` for separating major content sections on the screen.

## Elevation & Depth

Depth is communicated through a hybrid approach combining soft, ambient shadows and subtle low-contrast outlines. Surfaces are structured to lift interactive elements away from the warm off-white canvas without creating harsh visual clutter.

- **Surface Level 0 (Canvas):** `#F8F9FA` background with no elevation.
- **Surface Level 1 (Cards & Containers):** Pure white fill, subtle 1px border in a very light neutral gray, accompanied by a diffused, low-opacity shadow (`0px 4px 12px rgba(27, 67, 50, 0.06)`).
- **Surface Level 2 (Floating Action Buttons & Modals):** Enhanced ambient shadow (`0px 8px 24px rgba(27, 67, 50, 0.12)`) paired with a crisp primary forest-green or white background.

## Shapes

The design system uses a friendly rounded shape language (`roundedness: 2`), translating to a default 0.5rem (8px) border radius for standard components, 1rem (16px) for large cards (`rounded-lg`), and 1.5rem (24px) for prominent containers and bottom sheets (`rounded-xl`). 

Pill shapes are reserved exclusively for interactive chips, tags, and primary call-to-action buttons to reinforce touchability and modern mobile ergonomics.

## Components

### Buttons
Buttons feature generous padding and fully rounded pill shapes or soft 8px corners. Primary buttons use the deep forest-green fill with high-contrast white text. Secondary buttons utilize a subtle teal or outlined transparent style with forest-green borders and text.

### Chips & Tags
Compact, pill-shaped elements used for filtering community categories or status indicators. They feature a light tinted background derived from the teal highlight with dark charcoal text.

### Lists
Lists are presented within clean card containers or structured with generous vertical padding (minimum 12px per row). Dividers are subtle, 1px lines using a light neutral tint. Leading icons should use simple, consistent 24x24 stroke-based glyphs.

### Checkboxes & Radio Buttons
Custom-styled with rounded geometries. Selected states fill with the primary forest-green accent, featuring a smooth scale-in checkmark animation for tactile feedback.

### Input Fields
Text inputs feature an 8px border radius, a subtle neutral border, and a warm white or off-white background fill. Focus states transition smoothly to the teal highlight with a 2px stroke and a soft glow.

### Cards
Cards are the primary containers for content feeds, requests, and user profiles. They feature white surfaces, 12px to 16px border radii, thin borders, and soft ambient shadows to create clear, glanceable groupings.

### Bottom Sheets & Navigation
Mobile navigation relies on a bottom bar with clear iconography and label pairs. Bottom sheets slide up smoothly with a 24px top corner radius, anchored by a subtle drag handle at the top center.