/**
 * Created by abhishek on 7/30/17.
 */

import React, {Component} from 'react';
import {connect} from 'react-redux';
import {updateItem, selectItem} from  '../actions/index';
import {bindActionCreators} from 'redux';
const request = require('superagent');

export default class AddItemForm extends Component {
  render() {
    return (
        <div id="add-item-form">
          <div className="container-fluid">
            <div className="col-xs-12 col-md-4 col-md-offset-4">
              <div className="row form-group">
                <div className="col-xs-12">
                  <label htmlFor="page-url">URL:</label>
                  <input id="page-url" type="url" className="form-control" placeholder="URL"/>
                </div>
              </div>

              <div className="row form-group">
                <div className="col-xs-12">
                  <select name="role" id="role" className="form-control">
                    <option value="frontend">Front end engineer</option>
                    <option value="backend">Back end engineer</option>
                  </select>
                </div>
              </div>

              <div className="row form-group">
                <div className="col-xs-12">
                  <button className="btn btn-md btn-default">Load details</button>
                </div>
              </div>

            </div>
          </div>
        </div>
    )
  }
}