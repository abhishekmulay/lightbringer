/**
 * Created by abhishek on 7/14/17.
 */

import {CLEAR_ITEMS, ITEMS_FETCHED} from '../actions/index';

// state returned here will be an array
export default function (state = [], action) {
  console.log('[reducer] action received. state: ', state, ' action:', action);

  switch (action.type) {
    case ITEMS_FETCHED:
      // concat into new array => action.payload.body.hits.hits and state arrays
      console.log(ITEMS_FETCHED, action);
      const hits = (action.payload && action.payload.body &&
          action.payload.body.hits && action.payload.body.hits.hits) || [];
      return state.concat(hits);


    case CLEAR_ITEMS:
      return [];

    default:
      return state;
  }
}