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

export default function (state = [], action) {
  console.log('[reducer] action received. state: ', state, ' action:', action);

  switch (action.type) {
    case 'ITEMS_FETCHED':
      // concat into new array => action.payload.body.hits.hits and state arrays
      console.log('ITEMS_FETCHED', action);
      return state.concat(action.payload.body.hits.hits);

    default:
      return state;
  }
}