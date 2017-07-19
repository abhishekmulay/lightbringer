/**
 * Created by abhishek on 7/14/17.
 */

import {CLEAR_ITEMS, ITEMS_FETCHED, UPDATE_ITEM} from '../actions/index';

// state returned here will be an array
export default function (state = [], action) {
  console.log('[items reducer] state: ', state, ' action:', action);

  switch (action.type) {
    case ITEMS_FETCHED:
      // concat into new array => action.payload.body.hits.hits and state arrays
      const hits = (action.payload && action.payload.body &&
          action.payload.body.hits && action.payload.body.hits.hits) || [];
      return state.concat(hits);


    case UPDATE_ITEM:
      // update item in existing state array
      return state.map(function (item) {
        if (item._id === action.payload._id) {
          return action.payload; // return new item instead of older item
        }
        return item;
      });

    case CLEAR_ITEMS:
      return [];

    default:
      return state;
  }
}