/**
 * Created by abhishek on 7/14/17.
 */

export function selectItem(item) {
  // return Action
  return {
    type : 'ITEM_SELECTED',
    payload : item
  };
}

export function fetchItems(items) {
  return {
    type : 'ITEMS_FETCHED',
    payload: items
  };
}

export function appConfig(configObject) {
  return {
    type : 'APP_CONFIG',
    payload : configObject
  };
}