/**
 * Created by abhishek on 7/14/17.
 */
const request = require('superagent');

export const CLEAR_ITEMS = 'CLEAR_ITEMS';
export const ITEMS_FETCHED = 'ITEMS_FETCHED';
export const UPDATE_ITEM = 'UPDATE_ITEM';
export const ITEM_SELECTED = 'ITEM_SELECTED';
export const APP_CONFIG = 'APP_CONFIG';

export function selectItem(item) {
  return {
    type : ITEM_SELECTED,
    payload : item
  };
}

export function appConfig(configObject) {
  return {
    type : APP_CONFIG,
    payload : configObject
  };
}

// fetches items from API
export function fetchItems(searchTerm='', from = 0) {
  // console.log('[action] dispatching action ITEMS_FETCHED\n Fetching items for [' + searchTerm + ']');
  const url = 'http://localhost:4000/search';
  const promise = request
        .post(url)
        .send({ search_term : searchTerm, from : from }) // sends a JSON post body
        .set('Accept', 'application/json'); // return a promise at the end

  return {
    type: ITEMS_FETCHED,
    payload : promise
  }
}

// clear existing items array
export function clearItems() {
  return {
    type : CLEAR_ITEMS,
    payload : []
  };
}

export function updateItem(updatedItem) {
  return {
    type : UPDATE_ITEM,
    payload : updatedItem
  };
}