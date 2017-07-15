/**
 * Created by abhishek on 7/14/17.
 */

// reducers are called when action occurs.
export default function (state = null, action) {

  switch (action.type) {
    case 'ITEM_SELECTED':
      return action.payload;

    default:
      return state;
  }
}