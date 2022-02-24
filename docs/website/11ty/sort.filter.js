module.exports = (config) => {
  config.addFilter('sortByOrder', (values) => {
      return values.sort((a,b) => a.data.orderVal - b.data.orderVal)
  });
  
  /** Generic sort njk filter */
  const sortFilter = (comparer) => (values) => {
    if (!values || !Array.isArray(values)) {
      console.error(`Unexpected values for sort filter: ${values}`);
      return values;
    }
    return [...values].sort(comparer);
  };

  /** Comparer composer */
  const compose = (cmpA, cmpB) => (a, b) => cmpA(a, b) || cmpB(a, b);

  /** Name metadata comparer */
  const nameComparer = (a, b) => a.data.title.localeCompare(b.data.title);
  /** Order metadata comparer */
  const orderComparer = (a, b) => (a.data.order || 0) - (b.data.order || 0);

  config.addFilter('sortByName', sortFilter(nameComparer));
  config.addFilter('sortByNameAndOrder', sortFilter(compose(orderComparer, nameComparer)));
};
