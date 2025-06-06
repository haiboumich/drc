<template>
  <div :style="{padding: '1px 1px',height: '100%'}">
    <Table style="margin-top: 20px" stripe :columns="columns" :data="tableData" :loading="dataLoading" border
           ref="multipleTable">
      <template slot="action" slot-scope="{ row, index }">
        <Button type="primary" size="small" style="margin-right: 5px" @click="goToUpdateConfig(row, index)">修改
        </Button>
        <Button type="error" size="small" style="margin-right: 5px" @click="goToDeleteConfig(row, index)">删除
        </Button>
      </template>
    </Table>
    <br/>
    <Button type="success" @click="goToInsertConfig" icon="md-add">
      新增
    </Button>
    <Modal v-model="editModal.open" width="1200px" :footer-hide="true" :title="actionTitle">
      <db-mq-config ref="dbReplicationConfigComponent" @finished="finishConfig"
                             v-if="editModal.open"
                             :config-data="selectedRow"
                             :src-region="srcRegion"
                             :dst-region="srcRegion"
                             :db-names="dbNames"
                             :form-action="action"
      />
    </Modal>
  </div>
</template>

<script>
import DbMqConfig from '@/components/v2/dbDrcBuild/dbMqConfig.vue'

export default {
  name: 'mqTables',
  components: { DbMqConfig },
  props: {
    dbName: String,
    dbNames: Array,
    srcRegion: String,
    dstRegion: String,
    tableData: Array,
    dataLoading: Boolean
  },
  emits: ['updated'],
  data () {
    return {
      selectedRow: {},
      action: null,
      actionTitleMap: new Map([
        ['create', '新增 MQ投递配置'],
        ['edit', '更新 MQ投递配置'],
        ['delete', '⚠️删除 MQ投递配置']
      ]),
      editModal: {
        open: false
      },
      columns: [
        {
          title: '序号',
          width: 75,
          align: 'center',
          fixed: 'left',
          render: (h, params) => {
            return h(
              'span',
              params.index + 1
            )
          }
        },
        {
          title: '表名',
          key: 'config.logicTable',
          render: (h, params) => {
            const config = params.row.config
            return h(
              'span',
              config.logicTable
            )
          }
        },
        {
          title: '主题',
          key: 'topic',
          render: (h, params) => {
            const config = params.row.config
            return h(
              'span',
              config.dstLogicTable
            )
          }
        },
        {
          title: '有序',
          key: 'order',
          width: 100,
          render: (h, params) => {
            const row = params.row
            const color = 'blue'
            const text = row.order ? '有序' : '无序'
            return h('Tag', {
              props: {
                color: color
              }
            }, text)
          }
        },
        {
          title: '有序相关字段',
          key: 'orderKey'
        },
        {
          title: '操作',
          slot: 'action',
          align: 'center',
          fixed: 'right'
        }
      ]
    }
  },
  methods: {
    async goToInsertConfig () {
      this.selectedRow = null
      this.action = DbMqConfig.FORM_ACTION_OPTION.CREATE
      this.editModal.open = true
    },
    async goToUpdateConfig (row, index) {
      this.selectedRow = row
      this.action = DbMqConfig.FORM_ACTION_OPTION.EDIT
      this.editModal.open = true
    },
    async goToDeleteConfig (row, index) {
      this.selectedRow = row
      this.action = DbMqConfig.FORM_ACTION_OPTION.DELETE
      this.editModal.open = true
    },
    finishConfig () {
      this.selectedRow = null
      this.editModal.open = false
      this.$emit('updated')
    }
  },
  computed: {
    actionTitle () {
      return this.actionTitleMap.get(this.action)
    }
  },
  created () {
  }
}
</script>

<style scoped>

</style>
