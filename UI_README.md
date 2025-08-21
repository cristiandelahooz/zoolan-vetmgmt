# UI Configuration Guide

## MainLayout Design System

### Overview
The MainLayout uses Vaadin 24.8.3 with LineAwesome icons and Lumo utility classes for consistent, accessible, and maintainable UI design.

### Icon System

#### LineAwesome Integration
```java
// Import LineAwesome icons
import org.vaadin.lineawesome.LineAwesomeIcon;

// Create icons
Icon icon = LineAwesomeIcon.PAW_SOLID.create();
```

#### Available Icons for Veterinary Context
- `PAW_SOLID` - Brand/pets
- `STETHOSCOPE_SOLID` - Medical consultations
- `CALENDAR_ALT_SOLID` - Appointments
- `USER_FRIENDS_SOLID` - Clients
- `USERS_SOLID` - Employees
- `BOXES_SOLID` - Inventory
- `TRUCK_SOLID` - Suppliers
- `CLOCK_SOLID` - Waiting room
- `FILE_INVOICE_DOLLAR_SOLID` - Invoices
- `CLIPBOARD_LIST_SOLID` - Medical records

### Utility Classes

#### Spacing Scale
```java
// Padding
LumoUtility.Padding.XSMALL    // 0.25rem
LumoUtility.Padding.SMALL     // 0.5rem  
LumoUtility.Padding.MEDIUM    // 1rem
LumoUtility.Padding.LARGE     // 1.5rem
LumoUtility.Padding.XLARGE    // 2rem

// Margin (same scale)
LumoUtility.Margin.SMALL
LumoUtility.Margin.MEDIUM
// etc.
```

#### Typography Hierarchy
```java
// Font Sizes
LumoUtility.FontSize.SMALL     // 0.875rem
LumoUtility.FontSize.MEDIUM    // 1rem (base)
LumoUtility.FontSize.LARGE     // 1.125rem
LumoUtility.FontSize.XLARGE    // 1.25rem

// Font Weights
LumoUtility.FontWeight.NORMAL
LumoUtility.FontWeight.SEMIBOLD
LumoUtility.FontWeight.BOLD
```

#### Color System
```java
// Text Colors
LumoUtility.TextColor.HEADER      // Primary text
LumoUtility.TextColor.BODY        // Body text
LumoUtility.TextColor.SECONDARY   // Secondary text
LumoUtility.TextColor.TERTIARY    // Muted text

// Background Colors
LumoUtility.Background.BASE       // Default background
LumoUtility.Background.PRIMARY    // Brand color
LumoUtility.Background.CONTRAST_5 // Light contrast
```

#### Layout Utilities
```java
// Flexbox
LumoUtility.Display.FLEX
LumoUtility.AlignItems.CENTER
LumoUtility.JustifyContent.BETWEEN
LumoUtility.Flex.GROW

// Sizing
LumoUtility.Width.FULL
LumoUtility.Height.MEDIUM
LumoUtility.IconSize.MEDIUM
```

### Component Patterns

#### Navigation Item Creation
```java
private SideNavItem createNavItem(String label, String path, LineAwesomeIcon iconType) {
    Icon icon = iconType.create();
    icon.addClassNames(
        LumoUtility.IconSize.MEDIUM,
        LumoUtility.TextColor.SECONDARY
    );
    
    SideNavItem item = new SideNavItem(label, path, icon);
    item.addClassNames(
        LumoUtility.Padding.Vertical.XSMALL,
        LumoUtility.Padding.Horizontal.MEDIUM,
        LumoUtility.BorderRadius.MEDIUM,
        LumoUtility.Margin.Horizontal.SMALL
    );
    
    return item;
}
```

#### User Menu Pattern
```java
private HorizontalLayout createMenuItemWithIcon(LineAwesomeIcon iconType, String text) {
    Icon icon = iconType.create();
    icon.addClassNames(
        LumoUtility.IconSize.SMALL,
        LumoUtility.TextColor.SECONDARY
    );
    
    Span label = new Span(text);
    label.addClassNames(LumoUtility.Margin.Start.SMALL);
    
    HorizontalLayout layout = new HorizontalLayout(icon, label);
    layout.addClassNames(LumoUtility.AlignItems.CENTER);
    
    return layout;
}
```

### Responsive Design

#### Breakpoint Utilities
```java
// Max widths for responsive design
LumoUtility.MaxWidth.SCREEN_SMALL   // 640px
LumoUtility.MaxWidth.SCREEN_MEDIUM  // 768px
LumoUtility.MaxWidth.SCREEN_LARGE   // 1024px
LumoUtility.MaxWidth.SCREEN_XLARGE  // 1280px
```

#### Mobile-First Approach
- Use utility classes for base styles
- Add responsive variants as needed
- Leverage Vaadin's built-in responsive components

### Accessibility Guidelines

#### Semantic HTML
```java
// Use proper heading hierarchy
H1 pageTitle = new H1("Page Title");  // Main page title
H2 sectionTitle = new H2("Section");  // Section headers

// Proper landmarks
Header header = new Header();
Footer footer = new Footer();
```

#### ARIA Labels
```java
// Interactive elements
toggle.setAriaLabel("Menu toggle");
avatar.getElement().setAttribute("title", "User menu");
```

#### Color Contrast
- Use Lumo color tokens for WCAG compliance
- `TextColor.HEADER` for high contrast
- `TextColor.SECONDARY` for medium contrast
- `TextColor.TERTIARY` for low contrast (non-essential text)

### Theme Configuration

#### Enable Utility Classes
Ensure `theme.json` includes utility module:
```json
{
  "lumoImports": ["typography", "color", "spacing", "badge", "utility"]
}
```

#### Custom Properties
Access Lumo design tokens in CSS:
```css
.custom-component {
  color: var(--lumo-primary-text-color);
  padding: var(--lumo-space-m);
  border-radius: var(--lumo-border-radius-m);
}
```

### Performance Best Practices

#### Utility Class Benefits
- Reduced CSS bundle size
- Faster runtime styling
- Consistent design system
- Better caching

#### Component Reuse
- Create reusable component patterns
- Use utility classes for variations
- Minimize custom CSS

### Maintenance Guidelines

#### Adding New Views
1. Follow MainLayout patterns
2. Use consistent icon selection
3. Apply utility classes for styling
4. Maintain accessibility standards

#### Icon Updates
1. Check LineAwesome documentation for new icons
2. Maintain semantic consistency
3. Update icon mappings in navigation

#### Style Updates
1. Use utility classes over custom CSS
2. Follow Lumo design tokens
3. Test across different themes
4. Validate accessibility impact

### Testing Considerations

#### Visual Testing
- Utility classes provide consistent styling
- Test across different screen sizes
- Validate theme switching

#### Accessibility Testing
- Use semantic HTML structure
- Test keyboard navigation
- Validate screen reader compatibility
- Check color contrast ratios

### Common Patterns

#### Card Layout
```java
Div card = new Div();
card.addClassNames(
    LumoUtility.Background.BASE,
    LumoUtility.Padding.MEDIUM,
    LumoUtility.BorderRadius.MEDIUM,
    LumoUtility.BoxShadow.SMALL
);
```

#### Button Styling
```java
Button button = new Button("Action");
button.addClassNames(
    LumoUtility.Padding.Horizontal.MEDIUM,
    LumoUtility.Padding.Vertical.SMALL,
    LumoUtility.BorderRadius.MEDIUM
);
```

#### Form Layout
```java
FormLayout form = new FormLayout();
form.addClassNames(
    LumoUtility.Gap.MEDIUM,
    LumoUtility.Padding.MEDIUM
);
```

This design system provides a solid foundation for consistent, accessible, and maintainable UI development across the veterinary management application.