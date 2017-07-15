// reducers are called when action occurs.
export default function (state = null, action) {

  switch (action.type) {
    case 'APP_CONFIG':
      return action.payload;

    default:
      return state;
  }
}