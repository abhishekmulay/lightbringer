/**
 * Created by abhishek on 7/15/17.
 */

import React, { Component } from 'react';
import {connect} from 'react-redux';

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
                <h4 className="doc-title"><a href={this.props.item._source.url} target="_blank">{this.props.item._source.title}</a> </h4>
                <p className="text-muted">{this.props.item._source.author}</p>
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