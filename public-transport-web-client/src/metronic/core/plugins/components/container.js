import plugin from 'tailwindcss/plugin';

export default plugin(({ addComponents, theme }) => {
  // Fixed
  addComponents({
    '.container-fixed': {
      'flex-grow': '1',
      'width': '100%',
      'padding-inline-start': theme('custom.components.container.fixed.px.DEFAULT'), // Logical property for LTR/RTL
      'padding-inline-end': theme('custom.components.container.fixed.px.DEFAULT'),   // Logical property for LTR/RTL
    },
    [`@media (min-width: ${theme('screens.xl')})`]: {
      '.container-fixed': {
        'padding-inline-start': theme('custom.components.container.fixed.px.xl'),  // Logical property for LTR/RTL
        'padding-inline-end': theme('custom.components.container.fixed.px.xl'),    // Logical property for LTR/RTL
      },
    },
  });

  // Fluid
  addComponents({
    '.container-fluid': {
      'width': '100%',
      'flex-grow': '1',
      'padding-inline-start': theme('custom.components.container.fluid.px.DEFAULT'), // Logical property for LTR/RTL
      'padding-inline-end': theme('custom.components.container.fluid.px.DEFAULT'),   // Logical property for LTR/RTL
    },
    [`@media (min-width: ${theme('screens.xl')})`]: {
      '.container-fluid': {
        'padding-inline-start': theme('custom.components.container.fluid.px.xl'),   // Logical property for LTR/RTL
        'padding-inline-end': theme('custom.components.container.fluid.px.xl'),     // Logical property for LTR/RTL
      },
    },
  });
});
