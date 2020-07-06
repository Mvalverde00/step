class TreeNode {
  constructor(data) {
    this.data = data;
    this.children = [];
  }

  appendChild(treeNode) {
    this.children.push(treeNode);
  }
}
