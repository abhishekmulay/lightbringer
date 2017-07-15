/**
 * Created by abhishek on 7/15/17.
 */

import React, { Component } from 'react';
import {connect} from 'react-redux';
import {fetchItems} from  '../actions/index';
import {bindActionCreators} from 'redux';

class TitleBar extends Component {

  render() {
    if (!this.props.item) {
      return <div></div>
    }

    return (
        <div id="TitleBar">
          <div className="container-fluid">
            <div className="row">
              <div className="col-xs-12">
                <h4> {this.props.item._id}</h4>
              </div>
            </div>
          </div>
        </div>
    )
  }

}


function mapStateToProps(state) {
  return {
    item: state.activeItem
  }
}

export default connect(mapStateToProps)(TitleBar);