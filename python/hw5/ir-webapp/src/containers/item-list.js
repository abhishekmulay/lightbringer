/**
 * Created by abhishek on 7/14/17.
 */

import React, {Component} from 'react';
import {connect} from 'react-redux';
import {selectItem} from  '../actions/index';
import {bindActionCreators} from 'redux';

class ItemList extends Component {

  render() {
    if (!this.props.items) {
      return <h5 className="text-muted">Search something to see results.</h5>
    }
    return (
        <div id="ResultList">
          <ul className="list-group">
            {this.renderList()}
          </ul>
        </div>
    );
  }

  renderList() {
    return this.props.items.map((item) => {
      return (
          <li
              onClick={() => this.props.selectItem(item)}
              key={item['_id']}
              className="list-group-item">
            {item._id}
          </li>
      )
    });
  }
}

function mapStateToProps(state) {
  return {
    items: state.items
  }
}

// this will show up as props inside ItemList component
function mapDispatchToProps(dispatch) {
  // when selectItem is called, result should be passed to all our reducers
  return bindActionCreators({selectItem: selectItem}, dispatch);
}

//promote ItemList from a component to a container , it knows about new dispatch method selectItem
export default connect(mapStateToProps, mapDispatchToProps)(ItemList);