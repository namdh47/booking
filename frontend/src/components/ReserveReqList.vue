<template>

<v-data-table
    :headers="headers"
    :items="reserveReqList"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'ReserveReqList',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "matchId", value: "matchId" },
            { text: "price", value: "price" },
            { text: "startDay", value: "startDay" },
            { text: "endDay", value: "endDay" },
            { text: "name", value: "name" },
        ],
        reserveReqList : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/reservereqlists')

      this.reserveReqList = temp.data._embedded.reservereqlists;

    },
    methods: {
    }
  }
</script>

