/**
 * Created by abhishek on 7/14/17.
 */

import {ITEM_SELECTED} from "../actions/index";

// reducers are called when action occurs.
export default function (state = null, action) {
  console.log('[activeItem reducer] state:', state, ' action:', action);
  switch (action.type) {
    case ITEM_SELECTED:
      return action.payload;

    default:
      return state;
  }
}