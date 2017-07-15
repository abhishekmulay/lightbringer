/**
 * Created by abhishek on 7/14/17.
 */
//
// export default function () {
//   return [
//     {_id: 1, title: 'one'},
//     {_id: 2, title: 'two'},
//     {_id: 3, title: 'three'},
//     {_id: 4, title: 'four'},
//   ]
// }

export default function (state = null, action) {

  switch (action.type) {
    case 'ITEMS_FETCHED':
      return action.payload;

    default:
      return state;
  }
}