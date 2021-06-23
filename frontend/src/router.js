
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import ReserveManager from "./components/ReserveManager"

import LendManager from "./components/LendManager"

import ReserveReqList from "./components/ReserveReqList"
import PaymentManager from "./components/PaymentManager"

import MyPageManager from "./components/MyPageManager"

import MyPage from "./components/MyPage"
import SaleManager from "./components/SaleManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/reserves',
                name: 'ReserveManager',
                component: ReserveManager
            },

            {
                path: '/lends',
                name: 'LendManager',
                component: LendManager
            },

            {
                path: '/reserveReqLists',
                name: 'ReserveReqList',
                component: ReserveReqList
            },
            {
                path: '/payments',
                name: 'PaymentManager',
                component: PaymentManager
            },

            {
                path: '/myPages',
                name: 'MyPageManager',
                component: MyPageManager
            },

            {
                path: '/myPages',
                name: 'MyPage',
                component: MyPage
            },
            {
                path: '/sales',
                name: 'SaleManager',
                component: SaleManager
            },



    ]
})
