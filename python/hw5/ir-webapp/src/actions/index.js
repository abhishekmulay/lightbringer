/**
 * Created by abhishek on 7/14/17.
 */
const request = require('superagent');

export function selectItem(item) {
  // return Action
  return {
    type : 'ITEM_SELECTED',
    payload : item
  };
}

export function appConfig(configObject) {
  return {
    type : 'APP_CONFIG',
    payload : configObject
  };
}

export function fetchItems(searchTerm='') {
  console.log('[action] dispatching action ITEMS_FETCHED\n Fetching items for [' + searchTerm + ']');
  const url = 'http://localhost:4000/search';
  const response = request
        .post(url)
        .send({ search_term : searchTerm }) // sends a JSON post body
        .set('Accept', 'application/json');

  return {
    type: 'ITEMS_FETCHED',
    payload : response
  }
}