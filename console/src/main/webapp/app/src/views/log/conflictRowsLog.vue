<template>
  <div style="padding: 1px 1px ">
    <Row :gutter=10 align="middle">
      <Col span="20">
        <Card :padding=5>
          <template #title>查询条件</template>
          <Row :gutter=10 v-show="!searchMode">
            <Col span="2">
              <Select filterable clearable v-model="queryParam.srcRegion" placeholder="源region">
                <Option v-for="item in regions" :value="item.regionName" :key="item.regionName">
                  {{ item.regionName }}
                </Option>
              </Select>
            </Col>
            <Col span="2">
              <Select filterable clearable v-model="queryParam.dstRegion" placeholder="目标region">
                <Option v-for="item in regions" :value="item.regionName" :key="item.regionName">
                  {{ item.regionName }}
                </Option>
              </Select>
            </Col>
            <Col span="4">
              <Input prefix="ios-search" v-model="queryParam.dbName" placeholder="库名"></Input>
            </Col>
            <Col span="4">
              <Input prefix="ios-search" v-model="queryParam.tableName" placeholder="表名"></Input>
            </Col>
            <Col span="4">
              <DatePicker :transfer="true"  type="datetime" :editable="editable" v-model="queryParam.beginHandleTime"
                          :clearable="false" placeholder="起始日期"></DatePicker>
            </Col>
            <Col span="4">
              <DatePicker :transfer="true" type="datetime" :editable="editable" v-model="queryParam.endHandleTime"
                          :confirm="false" :clearable="false" placeholder="结束日期"></DatePicker>
            </Col>
            <Col span="2">
              <Select filterable clearable v-model="queryParam.brief" placeholder="日志情况">
                <Option v-for="item in briefOpts" :value="item.val" :key="item.val">{{ item.name }}</Option>
              </Select>
            </Col>
            <Col span="2">
              <Select filterable clearable v-model="queryParam.rowResult" placeholder="执行结果">
                <Option v-for="item in resultOpts" :value="item.val" :key="item.val">{{ item.name }}</Option>
              </Select>
            </Col>
          </Row>
          <Row :gutter=10 v-show="searchMode">
            <Col span="12">
              <Input prefix="ios-search" v-model="queryParam.gtid" placeholder="事务id"></Input>
            </Col>
            <Col span="4">
              <DatePicker type="datetime" :editable="editable" v-model="queryParam.beginHandleTime"
                          :clearable="false" placeholder="起始日期"></DatePicker>
            </Col>
            <Col span="4">
              <DatePicker type="datetime" :editable="editable" v-model="queryParam.endHandleTime"
                          :clearable="false" placeholder="结束日期"></DatePicker>
            </Col>
            <Col span="4">
              <Select filterable clearable v-model="queryParam.rowResult" placeholder="执行结果">
                <Option v-for="item in resultOpts" :value="item.val" :key="item.val">{{ item.name }}</Option>
              </Select>
            </Col>
          </Row>
        </Card>
      </Col>
      <Col span="4">
        <Row :gutter=10 align="middle">
          <Button type="primary" icon="ios-search" :loading="dataLoading" @click="getTotalData">查询</Button>
          <i-switch v-model="queryParam.likeSearch" size="large" style="margin-left: 10px">模糊匹配
            <template #open>
              <span>模糊匹配</span>
            </template>
            <template #close>
              <span>精确匹配</span>
            </template>
          </i-switch>
        </Row>
        <Row :gutter=10 align="middle" style="margin-top: 20px">
          <Button icon="md-refresh" @click="resetParam">重置</Button>
          <i-switch v-model="searchMode" size="large" @on-change="searchModeChange" style="margin-left: 10px">进阶
            <template #open>
              <span>进阶</span>
            </template>
            <template #close>
              <span>进阶</span>
            </template>
          </i-switch>
        </Row>
      </Col>
    </Row>
    <br>
    <Row style="background: #fdfdff; border: 1px solid #e8eaec;">
      <Col span="2" style="display: flex;float: left;margin: 5px">
        <Dropdown placement="bottom-start">
          <Button type="default" icon="ios-hammer">
            批量操作
            <Icon type="ios-arrow-down"></Icon>
          </Button>
          <template #list>
            <DropdownMenu>
              <DropdownItem @click.native="compareRecords">数据比对</DropdownItem>
              <DropdownItem @click.native="getLogDetails">冲突行详情</DropdownItem>
            </DropdownMenu>
          </template>
        </Dropdown>
      </Col>
      <Col span="2" style="display: flex;float: left;margin: 5px">
            <Button type="default" @click="dbBlacklist" icon="md-create">
              冲突黑名单
            </Button>
      </Col>
    </Row>
    <Table border :columns="columns" :data="tableData" ref="multipleTable"
           @on-selection-change="changeSelection">
      <template slot-scope="{ row, index }" slot="action">
        <Button type="primary" size="small" @click="getLogDetail1(row, index)" style="margin-right: 5px">
          详情
        </Button>
        <Button type="success" size="small" @click="queryTrxLog(row, index)" style="margin-right: 5px">
          冲突事务
        </Button>
      </template>
    </Table>
    <div style="text-align: center;margin: 16px 0">
      <Page
        v-if="!countLoading"
        :transfer="true"
        :total="total"
        :current.sync="current"
        :page-size-opts="[10,20,50,100]"
        :page-size="10"
        show-total
        show-sizer
        show-elevator
        @on-change="getData"
        @on-page-size-change="handleChangeSize"></Page>
      <div v-else>
        Total Loading...
      </div>
    </div>
    <Modal
      v-model="detailModal"
      title="冲突行详情"
      width="1200px" :scrollable="true" :draggable="true">
      <div :style="{padding: '1px 1px',height: '100%'}">
        <Card>
          <div class="ivu-list-item-meta-title">冲突行提交结果：
            <Button :loading="modalLoading" size="small" :type="logDetail.result==0?'success':'error'">
              {{logDetail.resultStr}}
            </Button>
          </div>
          <div class="ivu-list-item-meta-title">冲突行数据一致性比较结果：
            <Tooltip content="数据一致性比对忽略字段过滤的列">
              <Button :loading="modalLoading" size="small" :type="logDetail.recordEqual==true?'success':'warning'">
                {{logDetail.diffStr}}
              </Button>
            </Tooltip>
          </div>
          <Divider/>
          <div class="ivu-list-item-meta-title">源机房({{logDetail.srcRegion}})</div>
          <Card v-for="(item, index) in logDetail.srcRecords" :key="index">
            <div class="ivu-list-item-meta-title">表名：{{item.tableName}}
              <Tooltip :content="item.doubleSync==true?'双向同步':'单向同步'">
                <Button size="small" :type="item.doubleSync==true?'success':'primary'">
                  {{item.doubleSync == true ? '双向同步' : '单向同步'}}
                </Button>
              </Tooltip>
            </div>
            <Table :loading="modalLoading" size="small" :columns="item.columns" :data="item.records" border></Table>
          </Card>
          <Divider/>
          <div class="ivu-list-item-meta-title">目标机房({{logDetail.dstRegion}})</div>
          <Card v-for="(item, index) in logDetail.dstRecords" :key="index">
            <div class="ivu-list-item-meta-title">表名：{{item.tableName}}
              <Tooltip :content="item.doubleSync==true?'双向同步':'单向同步'">
                <Button size="small" :type="item.doubleSync==true?'success':'primary'">
                  {{item.doubleSync == true ? '双向同步' : '单向同步'}}
                </Button>
              </Tooltip>
            </div>
            <Table :loading="modalLoading" size="small" :columns="item.columns" :data="item.records" border></Table>
          </Card>
          <Divider/>
          <Card>
            <codemirror v-model="rowData" :options="options"></codemirror>
          </Card>
        </Card>
      </div>
    </Modal>
    <Modal
      v-model="compareModal"
      title="数据一致性比对结果"
      width="800px">
      <div v-if="this.rowLogIds.length>0" class="ivu-list-item-meta-title">存在数据比对不一致的冲突行，点击查询
        <Tooltip content="冲突行仅限当前页面">
          <Button size="middle" type="success" @click="getUnEqualRecords">冲突行</Button>
        </Tooltip>
      </div>
      <Table stripe border :loading="compareLoading" :columns="compareData.columns"
             :data="compareData.compareRowRecords">
      </Table>
    </Modal>
  </div>
</template>

<script>
import { codemirror } from 'vue-codemirror'
import 'codemirror/theme/ambiance.css'
import 'codemirror/mode/sql/sql.js'

export default {
  name: 'conflictRowsLog',
  props: {
    gtid: String,
    beginHandleTime: String,
    endHandleTime: String,
    searchMode: Boolean
  },
  components: {
    codemirror
  },
  data () {
    return {
      compareModal: false,
      multiData: [],
      detailModal: false,
      compareEqualLoading: false,
      modalLoading: false,
      compareLoading: false,
      rowLogIds: [],
      compareData: {
        compareRowRecords: [],
        columns: [
          {
            title: '表名',
            key: 'tableName'
          },
          {
            title: '比对结果',
            key: 'recordIsEqual',
            render: (h, params) => {
              const row = params.row
              const color = row.recordIsEqual ? 'blue' : 'warning'
              const text = row.recordIsEqual ? '数据一致' : '数据不一致'
              return h('Tag', {
                props: {
                  color: color
                }
              }, text)
            }
          }
        ]
      },
      logDetail: {
        srcRecords: [],
        dstRecords: [],
        srcRegion: '',
        dstRegion: '',
        result: null,
        resultStr: '',
        hasDiff: null,
        recordEqual: null,
        diffStr: '',
        rowData: ''
      },
      regions: [],
      // searchMode: this.searchMode1,
      editable: false,
      dataLoading: false,
      countLoading: false,
      queryParam: {
        srcRegion: this.$route.query.srcRegion,
        dstRegion: this.$route.query.dstRegion,
        dbName: this.$route.query.dbName,
        tableName: this.$route.query.tableName,
        rowResult: this.$route.query.rowResult ? Number(this.$route.query.rowResult) : null,
        brief: this.$route.query.brief ? Number(this.$route.query.brief) : null,
        gtid: this.gtid,
        likeSearch: this.$route.query.likeSearch === true || this.$route.query.likeSearch === 'true',
        beginHandleTime: this.$route.query.beginTime ? new Date(Number(this.$route.query.beginTime)) : this.beginHandleTime,
        endHandleTime: this.$route.query.endTime ? new Date(Number(this.$route.query.endTime)) : this.endHandleTime
      },
      tableData: [],
      columns: [
        {
          type: 'selection',
          width: 60,
          align: 'center'
        },
        {
          title: '同步方向',
          key: 'region',
          width: 160,
          render: (h, params) => {
            const row = params.row
            const color = 'blue'
            const text = row.srcRegion + ' -> ' + row.dstRegion
            return h('Tag', {
              props: {
                color: color
              }
            }, text)
          }
        },
        {
          title: '表名',
          key: 'tableName',
          width: 250,
          tooltip: true,
          render: (h, params) => {
            const row = params.row
            const text = row.dbName + '.' + row.tableName
            return h('div', text)
          }
        },
        {
          title: '原始sql',
          key: 'rawSql',
          tooltip: true
        },
        {
          title: '冲突处理sql',
          key: 'handleSql',
          tooltip: true
        },
        {
          title: '提交时间',
          key: 'handleTime',
          width: 180,
          sortable: true
        },
        {
          title: '执行结果',
          key: 'rowResult',
          width: 150,
          align: 'center',
          render: (h, params) => {
            const row = params.row
            const color = row.rowResult === 0 ? 'blue' : 'volcano'
            const text = row.rowResult === 0 ? 'commit' : 'rollBack'
            return h('Tag', {
              props: {
                color: color
              }
            }, text)
          }
        },
        {
          title: '数据是否一致',
          key: 'recordIsEqual',
          width: 150,
          align: 'center',
          renderHeader: (h, params) => {
            return h('span', [
              h('span', '数据比对'),
              h('Button', {
                on: {
                  click: async () => {
                    await this.compareRecordEqual()
                  }
                },
                props: {
                  loading: this.compareEqualLoading,
                  size: 'small',
                  shape: 'circle',
                  type: 'default',
                  icon: 'md-refresh'
                }
              })
            ])
          },
          render: (h, params) => {
            const row = params.row
            let color, text
            if (row.recordIsEqual === true) {
              color = 'success'
              text = '一致'
            } else if (row.recordIsEqual === false) {
              color = 'warning'
              text = '不一致'
            }
            return h('Tag', {
              props: {
                color: color
              }
            }, text)
          }
        },
        {
          title: '操作',
          slot: 'action',
          width: 165,
          align: 'center'
        }
      ],
      total: 0,
      current: 1,
      size: 10,
      pageSizeOpts: [10, 20, 50, 100],
      resultOpts: [
        {
          name: 'commit',
          val: 0
        },
        {
          name: 'rollBack',
          val: 1
        }
      ],
      briefOpts: [
        {
          name: '有日志',
          val: 0
        },
        {
          name: '无日志',
          val: 1
        }
      ],
      options: {
        value: '',
        mode: 'text/x-mysql',
        theme: 'ambiance',
        lineWrapping: true,
        height: 100,
        readOnly: true,
        lineNumbers: true
      }
    }
  },
  methods: {
    resetPath () {
      if (!this.searchMode) {
        this.$router.replace({
          query: {
            srcRegion: this.queryParam.srcRegion,
            dstRegion: this.queryParam.dstRegion,
            dbName: this.queryParam.dbName,
            tableName: this.queryParam.tableName,
            rowResult: this.queryParam.rowResult,
            brief: this.queryParam.brief,
            likeSearch: this.queryParam.likeSearch,
            beginTime: new Date(this.queryParam.beginHandleTime).getTime(),
            endTime: new Date(this.queryParam.endHandleTime).getTime()
          }
        })
      } else {
        this.$router.replace({
          query: {
            gtid: this.queryParam.gtid,
            rowResult: this.queryParam.rowResult,
            beginTime: new Date(this.queryParam.beginHandleTime).getTime(),
            endTime: new Date(this.queryParam.endHandleTime).getTime()
          }
        })
      }
    },
    searchModeChange () {
      this.queryParam.gtid = null
    },
    compareRecordEqual () {
      const rowLogIds = []
      if (this.tableData.length === 0) {
        return
      }
      this.tableData.forEach(data => rowLogIds.push(data.conflictRowsLogId))
      this.compareEqualLoading = true
      this.axios.get('/api/drc/v2/log/conflict/compare?conflictRowLogIds=' + rowLogIds)
        .then(response => {
          if (response.data.status === 1) {
            this.$Message.error({
              content: '数据比对失败! ' + response.data.message,
              duration: 5
            })
          } else {
            const data = response.data.data
            const dataMap = new Map(data.map(e => [e.rowLogId, e.recordIsEqual]))
            this.tableData.forEach(line => {
              line.recordIsEqual = dataMap.get(line.conflictRowsLogId)
            })
          }
        })
        .finally(() => {
          this.compareEqualLoading = false
        })
    },
    compareRecords () {
      const multiData = this.multiData
      if (multiData === undefined || multiData === null || multiData.length === 0) {
        this.$Message.warning('请勾选！')
        return
      }
      this.compareLoading = true
      this.compareModal = true
      const rowLogIds = []
      multiData.forEach(data => rowLogIds.push(data.conflictRowsLogId))
      this.axios.get('/api/drc/v2/log/conflict/records/compare?conflictRowLogIds=' + rowLogIds)
        .then(response => {
          if (response.data.status === 1) {
            this.compareData.compareRowRecords = []
            this.$Message.error({
              content: '数据比对失败! ' + response.data.message,
              duration: 5
            })
          } else {
            const data = response.data.data
            this.compareData.compareRowRecords = data.recordDetailList
            this.rowLogIds = []
            data.rowLogIds.forEach(e => this.rowLogIds.push(e))
          }
        })
        .finally(() => {
          this.compareLoading = false
        })
    },
    getLogDetails () {
      const multiData = this.multiData
      if (multiData === undefined || multiData === null || multiData.length === 0) {
        this.$Message.warning('请勾选！')
        return
      }
      const rowLogIds = []
      const row = multiData[0]
      multiData.forEach(data => rowLogIds.push(data.conflictRowsLogId))
      this.axios.get('/api/drc/v2/log/conflict/rows/check?conflictRowLogIds=' + rowLogIds)
        .then(response => {
          if (response.data.status === 1) {
            this.$Message.error(response.data.message)
          } else {
            const detail = this.$router.resolve({
              path: '/conflictLogDetail',
              query: {
                queryType: '1',
                rowLogIds: rowLogIds,
                srcRegion: row.srcRegion,
                dstRegion: row.dstRegion
              }
            })
            window.open(detail.href, '_blank')
          }
        })
    },
    dbBlacklist () {
      const detail = this.$router.resolve({
        path: '/dbBlacklist'
      })
      window.open(detail.href, '_blank')
    },
    changeSelection (val) {
      this.multiData = val
      console.log(this.multiData)
    },
    getLogDetail1 (row, index) {
      this.modalLoading = true
      this.detailModal = true
      this.logDetail.result = row.rowResult
      this.logDetail.resultStr = row.rowResult === 0 ? 'commit' : 'rollBack'
      this.logDetail.srcRegion = row.srcRegion
      this.logDetail.dstRegion = row.dstRegion
      this.logDetail.srcRecords = []
      this.logDetail.dstRecords = []
      this.rowData = ''
      this.axios.get('/api/drc/v2/log/conflict/row/record?conflictRowLogId=' + row.conflictRowsLogId + '&columnSize=12')
        .then(response => {
          if (response.data.status === 1) {
            this.$Message.error({
              content: '查询失败! ' + response.data.message,
              duration: 5
            })
            // this.$Message.error(response.data.message)
            this.logDetail.recordEqual = false
            this.logDetail.diffStr = '数据比对失败'
            this.logDetail.srcRecords = []
            this.logDetail.dstRecords = []
          } else {
            const data = response.data.data
            this.logDetail.recordEqual = data.recordIsEqual
            this.logDetail.diffStr = data.recordIsEqual ? '数据一致' : '数据不一致'
            this.logDetail.srcRecords = data.srcRecords
            this.logDetail.dstRecords = data.dstRecords
            this.rowData = '/*原始SQL*/\n' + row.rawSql + '\n/*原始SQL处理结果: ' + row.rawSqlResult + '*/\n\n' + '/*冲突时行记录*/\n' +
              row.dstRowRecord + '\n\n' + '/*冲突处理SQL*/\n' + row.handleSql + '\n/*冲突处理SQL处理结果: ' + row.handleSqlResult + '*/'
          }
        })
        .finally(() => {
          this.modalLoading = false
        })
    },
    getRegions () {
      this.axios.get('/api/drc/v2/meta/regions/all')
        .then(response => {
          this.regions = response.data.data
        })
    },
    queryTrxLog (row, index) {
      this.$emit('tabValueChanged', 'trxLog')
      this.$emit('gtidChanged', row.gtid)
      this.$emit('beginHandleTimeChanged', this.queryParam.beginHandleTime)
      this.$emit('endHandleTimeChanged', this.queryParam.endHandleTime)
      // this.tabVal = 'rowsLog'
    },
    getUnEqualRecords () {
      this.multiData = []
      this.compareModal = false
      this.dataLoading = true
      this.axios.get('/api/drc/v2/log/conflict/rows/rowLogIds?rowLogIds=' + this.rowLogIds)
        .then(response => {
          const data = response.data
          if (data.status === 1) {
            this.$Message.error('查询失败')
          } else {
            this.tableData = data.data
            this.total = data.data.length
            this.current = 1
            this.tableData = data.data
            if (this.total === 0) {
              this.$Message.warning('查询结果为空')
            } else {
              this.compareRecordEqual()
              this.$Message.success('以下冲突行数据对比不一致')
            }
          }
        })
        .finally(() => {
          this.dataLoading = false
        })
    },
    getTotalData () {
      this.getData()
      this.getCount()
    },
    getCount () {
      const beginTime = this.queryParam.beginHandleTime
      const endTime = this.queryParam.endHandleTime
      const beginHandleTime = new Date(beginTime).getTime()
      const endHandleTime = new Date(endTime).getTime()
      if (isNaN(beginHandleTime) || isNaN(endHandleTime)) {
        return
      }
      console.log('beginTime: ' + beginTime)
      console.log('endTime: ' + endTime)
      const params = {
        gtid: this.queryParam.gtid,
        dbName: this.queryParam.dbName,
        tableName: this.queryParam.tableName,
        rowResult: this.queryParam.rowResult,
        srcRegion: this.queryParam.srcRegion,
        dstRegion: this.queryParam.dstRegion,
        beginHandleTime: beginHandleTime,
        endHandleTime: endHandleTime,
        likeSearch: this.queryParam.likeSearch,
        brief: this.queryParam.brief,
        pageReq: {
          pageSize: this.size,
          pageIndex: this.current
        }
      }
      this.countLoading = true
      const reqParam = this.flattenObj(params)
      this.axios.get('/api/drc/v2/log/conflict/rows/count', { params: reqParam })
        .then(response => {
          const data = response.data
          if (data.status === 0) {
            this.total = data.data
          }
        })
        .finally(() => {
          this.countLoading = false
        })
    },
    getData () {
      this.resetPath()
      this.multiData = []
      this.compareData.compareRowRecords = []
      this.rowLogIds = []
      const beginTime = this.queryParam.beginHandleTime
      const endTime = this.queryParam.endHandleTime
      const beginHandleTime = new Date(beginTime).getTime()
      const endHandleTime = new Date(endTime).getTime()
      if (isNaN(beginHandleTime) || isNaN(endHandleTime)) {
        this.$Message.warning('请选择时间范围!')
        return
      }
      // const beginHandleTime = beginTime === null || isNaN(beginTime) ? null : new Date(beginTime).getTime()
      // const endHandleTime = endTime === null || isNaN(endTime) ? null : new Date(endTime).getTime()
      console.log('beginTime: ' + beginTime)
      console.log('endTime: ' + endTime)
      const params = {
        gtid: this.queryParam.gtid,
        dbName: this.queryParam.dbName,
        tableName: this.queryParam.tableName,
        rowResult: this.queryParam.rowResult,
        srcRegion: this.queryParam.srcRegion,
        dstRegion: this.queryParam.dstRegion,
        beginHandleTime: beginHandleTime,
        endHandleTime: endHandleTime,
        likeSearch: this.queryParam.likeSearch,
        brief: this.queryParam.brief,
        pageReq: {
          pageSize: this.size,
          pageIndex: this.current
        }
      }
      console.log('params')
      console.log(params)
      const reqParam = this.flattenObj(params)
      this.dataLoading = true
      this.axios.get('/api/drc/v2/log/conflict/rows', { params: reqParam })
        .then(response => {
          const data = response.data
          const pageResult = data.pageReq
          if (data.status === 1) {
            this.$Message.error(data.message)
          } else if (data.data.length === 0) {
            // this.total = 0
            this.current = 1
            this.tableData = data.data
            this.$Message.warning('查询结果为空')
          } else {
            // this.total = pageResult.totalCount
            this.current = pageResult.pageIndex
            this.tableData = data.data
            this.$Message.success('查询成功')
            this.compareRecordEqual()
          }
        })
        .finally(() => {
          this.dataLoading = false
        })
    },
    flattenObj (ob) {
      const result = {}
      for (const i in ob) {
        if ((typeof ob[i]) === 'object' && !Array.isArray(ob[i])) {
          const temp = this.flattenObj(ob[i])
          for (const j in temp) {
            result[i + '.' + j] = temp[j]
          }
        } else {
          result[i] = ob[i]
        }
      }
      return result
    },
    resetParam () {
      this.queryParam = {
        dbName: null,
        tableName: null,
        gtId: null,
        beginHandleTime: new Date(new Date().setSeconds(0, 0) - 10 * 60 * 1000),
        endHandleTime: new Date(new Date().setSeconds(0, 0) + 60 * 1000),
        rowResult: null,
        srcRegion: null,
        dstRegion: null
      }
      // this.countLoading = false
      this.rowLogIds = []
    },
    handleChangeSize (val) {
      this.size = val
      this.$nextTick(() => {
        this.getData()
      })
    }
  },
  created () {
    this.getTotalData()
    this.getRegions()
  }
}
</script>

<style>
.ivu-table .cell-class-type {
  background-color: #2db7f5;
  color: #fff;
}
</style>
