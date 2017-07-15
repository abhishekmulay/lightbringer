/**
 * Created by abhishek on 7/14/17.
 */

import React, {Component} from 'react';
import {connect} from 'react-redux';

class ItemDetail extends Component {
  render() {
    if (!this.props.item) {
      return <h4 className="text-muted">Select a result from list to get more information.</h4>
    }

    return (
        <div id="details">
          <div className="container-fluid">
            <div className="row">
              <div className="col-xs-12">
                <h4>{this.props.item._id}</h4>
              </div>
              <div className="col-xs-12">
                <button className="btn btn-default" onClick={()=> console.log(this.props)}>Item</button>
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

export default connect(mapStateToProps)(ItemDetail);